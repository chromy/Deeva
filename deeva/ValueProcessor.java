package deeva;

import com.sun.jdi.*;
import deeva.processor.*;
import deeva.processor.ClassValue;

import java.util.*;

public class ValueProcessor {
    public static JVMValue processVariable(LocalVariable localVariable,
                                           Value variableValue,
                                           Set<String> sources) {
        if (localVariable == null) {
            return null;
        }

        JVMValue jvmValue = processValueOverview(variableValue, sources);
        jvmValue.setName(localVariable.name());

        return jvmValue;
    }

    public static JVMValue processValueOverview(Value variableValue,
                                                Set<String> sources) {
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
                ClassValue classValue
                        = new ClassValue(typeName, uniqueID, objRef, true);
                classValue.setFields(processFields(classValue, objRef, sources));
                value = classValue;
            }
        } else {
            value = null;
        }

        return value;
    }

    private static List<JVMField> processFields(ClassValue objOverview,
                                                ObjectReference variableValue,
                                                Set<String> sources) {
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

                JVMValue jvmValue = processValueOverview(value, sources);

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
        return fields;
    }

    public static JVMValue processValueFull(Value variableValue,
                                            Set<String> sources) {
        JVMValue objOverview = processValueOverview(variableValue, sources);

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
                array[i] = processValueOverview(arrRefList.get(i), sources);
            }

            /* Set the arrayValue to contain this new array */
            arrVal.setArray(array);

            return arrVal;
        }

        return objOverview;
    }


}
