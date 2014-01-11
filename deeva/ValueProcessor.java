package deeva;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jdi.*;
import deeva.processor.*;
import deeva.processor.ClassValue;

import java.util.*;

public class ValueProcessor {
    public static JVMValue processVariable(LocalVariable localVariable,
                                                      Value variableValue) {
        if (localVariable == null) {
            return null;
        }

        JVMValue jvmValue = processValueOverviewNew(variableValue);
        jvmValue.setName(localVariable.name());

        return jvmValue;
    }

    public static Map<String, ? extends Object>
        processValueSingleDepth(Value variableValue, Set<String> sources) {
        Map<String, ? extends Object> overview
                = processValueOverview(variableValue);

        /* SingleDepth is a superset of the overview, so populate initial map
         with that  */
        Map<String, Object> varMap = new HashMap<String, Object>(overview);

        /* A traversal only applies if we're in an reference type */
        if (!(variableValue instanceof ObjectReference)) {
            return varMap;
        }

        JVMValue jvmValue = processValueFull(variableValue, sources);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jvmValue);
        System.err.println("JSON FULL OBJECT:");
        System.err.println(json);
        System.err.println("Done printing JSON");

        ObjectReference objRef = (ObjectReference) variableValue;

            /* Deal with arrays */
        if (variableValue instanceof ArrayReference) {
            ArrayReference arrRef = (ArrayReference) variableValue;

            List<Map<String, String>> arrayList = new
                    ArrayList<Map<String, String>>();

            List<Value> arrRefList = arrRef.getValues();

            /* Get overview of each of the values */
            for (Value val : arrRefList) {
                arrayList.add(ValueProcessor.processValueOverview(val));
            }

            /* Add to map */
            varMap.put("array", arrayList);
        } else if (!(variableValue instanceof StringReference)) {
            /* Deal with objects other than String */
            /* TODO: Need to deal with enums */
            ClassType classType = (ClassType) objRef.type();

            String typeName = classType.name();
            List<Map<String, String>> fields
                    = new LinkedList<Map<String, String>>();
            Set<String> seenFields = new HashSet<String>();


            ClassType currentClassType = classType;
            /* Get all the fields in the current class and all the user
            created classes */
            while (sources.contains(typeName)) {
                List<Field> fieldList = currentClassType.fields();

                /* For each field we find, we add to the fields map */
                for (Field field : fieldList) {
                    /* If we've already seen the field in a subclass we
                    don't override it. */
                    String fieldName = field.name();
                    if (fields.contains(fieldName) || field.isSynthetic()) {
                        continue;
                    }

                    /*  TODO: Get a cached result of static information about
                        the field
                     */

                    /* Process the value */
                    Value value = objRef.getValue(field);

                    Map<String, String> processedValue =
                            ValueProcessor.processValueOverview(value);
                    if (value == null) {
                        processedValue.put("type", field.typeName());
                    }

                    Boolean isFinal = field.isFinal();
                    Boolean isStatic = field.isStatic();

                    /* Accessibility */
                    String accesibility = "";
                    if(field.isPrivate()) {
                        accesibility = "private";
                    } else if(field.isProtected()) {
                        accesibility = "protected";
                    } else if (field.isPackagePrivate()) {
                        accesibility = "package-private";
                    } else if (field.isPublic()) {
                        accesibility = "public";
                    }

                    processedValue.put("name", fieldName);
                    processedValue.put("final", isFinal.toString());
                    processedValue.put("static", isStatic.toString());
                    processedValue.put("accessibility", accesibility);

                    fields.add(processedValue);
                    seenFields.add(fieldName);
                }
                currentClassType = currentClassType.superclass();
                typeName = currentClassType.name();
            }

            /* Add to map */
            varMap.put("fields", fields);
        }
        return varMap;
    }

    public static JVMValue processValueOverviewNew(Value variableValue) {
        if (variableValue == null) {
            return null;
        }

        Type valueType = variableValue.type();
        String typeName = valueType.name();
        JVMValue value;

        if (variableValue instanceof com.sun.jdi.PrimitiveValue) {
            /* Deal with primitive values */
            value = new deeva.processor.PrimitiveValue(typeName, variableValue);
        } else if (variableValue instanceof ObjectReference) {
            /* Deal with reference types */
            ObjectReference objRef = (ObjectReference) variableValue;
            Long uniqueID = objRef.uniqueID();

            if (variableValue instanceof StringReference) {
                StringReference strRef = (StringReference) variableValue;
                value = new StringValue(typeName, uniqueID, strRef);
            } else if(variableValue instanceof ArrayReference) {
                ArrayReference arrRef = (ArrayReference) variableValue;
                value = new ArrayValue(typeName, uniqueID, arrRef, true);
            } else {
                value = new ClassValue(typeName, uniqueID, objRef, true);
            }
        } else {
            value = null;
        }

        return value;
    }

    public static JVMValue processValueFull(Value variableValue,
                                            Set<String> sources) {
        JVMValue objOverview = processValueOverviewNew(variableValue);

        if (!(variableValue instanceof ObjectReference)) {
            /* TODO: Better error handling*/
            return objOverview;
        }

        if (variableValue instanceof ArrayReference) {
            ArrayValue arrVal = (ArrayValue) objOverview;
            ArrayReference arrRef = (ArrayReference) variableValue;

            /* Values in the array from JDI*/
            List<Value> arrRefList = arrRef.getValues();
            int numElems = arrRefList.size();

            JVMValue[] array = new JVMValue[numElems];

            /* Add an overview of each element in the array */
            for (int i = 0; i < numElems; i++) {
                array[i] = processValueOverviewNew(arrRefList.get(i));
            }

            /* Set the arrayValue to contain this new array */
            arrVal.setArray(array);

            return arrVal;
        } else if (!(variableValue instanceof StringReference)) {
            ClassValue classVal = (ClassValue) objOverview;
            ObjectReference objRef = (ObjectReference) variableValue;
            ClassType classType = (ClassType) objRef.type();
            String typeName = classType.name();
            Set<String> seenFields = new HashSet<String>();
            List<JVMField> fields = new LinkedList<JVMField>();

            ClassType currentClassType = classType;
            /* Get all the fields in the current class and al the user
            created classes */
            while(sources.contains(typeName)) {
                List<Field> fieldList = currentClassType.fields();

                /* For each field we find, we add to the fields list */
                for (Field field : fieldList) {
                    String fieldName = field.name();
                    /* If we've already seen this field i.e. the current
                    field is a field declared in a superclass that has also
                    been overriden in a descendant class OR the field has
                    been generated by the compiler, we skip */
                    if (seenFields.contains(fieldName) || field.isSynthetic()) {
                        continue;
                    }

                    /* Process the value */
                    Value value = objRef.getValue(field);

                    JVMValue jvmValue = processValueOverviewNew(value);

                    boolean isFinal = field.isFinal();
                    boolean isStatic = field.isStatic();

                    JVMField.Accessibility accessibility;
                    if (field.isPrivate()) {
                        accessibility = JVMField.Accessibility.PRIVATE;
                    } else if (field.isProtected()) {
                        accessibility = JVMField.Accessibility.PROTECTED;
                    } else if (field.isPackagePrivate()) {
                        accessibility = JVMField.Accessibility.PACKAGE_PRIVATE;
                    } else {
                        accessibility = JVMField.Accessibility.PUBLIC;
                    }

                    JVMField jvmField = new JVMField(fieldName, jvmValue,
                            accessibility, isFinal, isStatic);

                    fields.add(jvmField);
                    seenFields.add(fieldName);
                }

                currentClassType = currentClassType.superclass();
                typeName = currentClassType.name();
            }

            /* Set the fields element */
            classVal.setFields(fields);

            return classVal;
        }

        return objOverview;
    }

    public static Map<String, String> processValueOverview(Value variableValue) {
        Map<String, String> varMap = new HashMap<String, String>();

        /* Handle null objects */
        if (variableValue == null) {
            varMap.put("value", "null");
            return varMap;
        }

        Type valueType = variableValue.type();
        String typeName = valueType.name();
        varMap.put("type", typeName);

        /* We deal with primitive types */
        if (valueType instanceof IntegerType) {
            Integer value = ((IntegerValue) variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof BooleanType) {
            Boolean value = ((BooleanValue) variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof ByteType) {
            Byte value = ((ByteValue) variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof CharType) {
            Character value = ((CharValue) variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof DoubleType) {
            Double value = ((DoubleValue) variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof FloatType) {
            Float value = ((FloatValue) variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof LongType) {
            Long value = ((LongValue) variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof ShortType) {
            Short value = ((ShortValue) variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof VoidType) {
            varMap.put("value", "void");
        }

        /* Deal with object references */
        else if (variableValue instanceof ObjectReference) {
            /* Store the unique id, intrinsic to every object */
            ObjectReference objRef = (ObjectReference) variableValue;
            Long uniqueID = objRef.uniqueID();
            varMap.put("unique_id", uniqueID.toString());
            varMap.put("object_type", "object");

            /* Deal with Strings and Arrays (overviews) */
            if (variableValue instanceof StringReference) {
                StringReference strRef = (StringReference) variableValue;
                varMap.put("string", strRef.value());
                varMap.put("object_type", "string");
            } else if (variableValue instanceof ArrayReference) {
                varMap.put("object_type", "array");
                ArrayReference arrRef = (ArrayReference) variableValue;

                /* Get the component type */
                ArrayType arrType = (ArrayType) arrRef.type();
                varMap.put("component_type", arrType.componentTypeName());


                /* Get the length */
                Integer length = arrRef.length();
                varMap.put("length", length.toString());

            }
        }

        return varMap;
    }
}
