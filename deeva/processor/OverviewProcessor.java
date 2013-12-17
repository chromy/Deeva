package deeva.processor;

import com.sun.jdi.*;

import java.util.HashMap;
import java.util.Map;

public class OverviewProcessor {
    public static Map<String, String> processVariable(LocalVariable localVariable, Value variableValue) {
        if (localVariable == null) {
            return null;
        }
        
        Map<String, String> localVarMap = new HashMap<String, String>();
        localVarMap.put("name", localVariable.name());

        /* Now process the actual variable value */
        Map<String, String> varMapOverview = OverviewProcessor.processValue(variableValue);
        localVarMap.putAll(varMapOverview);

        return localVarMap;
    }

    public static Map<String, String> processValue(Value variableValue) {
        Map<String, String> varMap = new HashMap<String, String>();

        Type valueType = variableValue.type();
        varMap.put("type", valueType.name());

        /* We deal with primitive types */
        if (valueType instanceof IntegerType) {
            Integer value = ((IntegerValue)variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof BooleanType) {
            Boolean value = ((BooleanValue)variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof ByteType) {
            Byte value = ((ByteValue)variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof CharType) {
            Character value = ((CharValue)variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof DoubleType) {
            Double value = ((DoubleValue)variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof FloatType) {
            Float value = ((FloatValue)variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof LongType) {
            Long value = ((LongValue)variableValue).value();
            varMap.put("value", value.toString());
        } else if (valueType instanceof ShortType) {
            Short value = ((ShortValue)variableValue).value();
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
            }
        }

        return varMap;
    }
}
