package deeva.processor;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;

public class ArrayValue extends ReferenceValue {
    private final String component_type;
    private final int length;
    private JVMValue[] array;

    public ArrayValue(String type, long unique_id, ArrayReference arrRef,
                      boolean overview) {
        super(type, unique_id, ObjectType.ARRAY);

        ArrayType arrType = (ArrayType) arrRef.type();
        this.component_type = arrType.componentTypeName();
        this.length = arrRef.length();

        if (!overview) {
            this.array = null;
        }
    }

    public String getComponentType() {
        return component_type;
    }

    public int getLength() {
        return length;
    }

    public JVMValue[] getArray() {
        return array;
    }

    public void setArray(JVMValue[] array) {
        this.array = array;
    }
}
