package deeva;

import com.sun.jdi.*;

import java.util.*;

public class ValueProcessor {
    public static Map<String, String> processVariable(LocalVariable localVariable,
                                                      Value variableValue,
                                                      Map<String, String>sources) {
        if (localVariable == null) {
            return null;
        }

        Map<String, String> localVarMap = new HashMap<String, String>();
        localVarMap.put("name", localVariable.name());

        /* Now process the actual variable value */
        Map<String, String> varMapOverview =
                ValueProcessor.processValueOverview(variableValue);
        localVarMap.putAll(varMapOverview);

        return localVarMap;
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

    public static Map<String, String> processValueOverview(Value variableValue) {
        Map<String, String> varMap = new HashMap<String, String>();

        /* Handle null objects */
        if (variableValue == null) {
            varMap.put("value", "null");
            return varMap;
        }

        Type valueType = variableValue.type();
        varMap.put("type", valueType.name());

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
