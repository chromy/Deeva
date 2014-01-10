package deeva.processor;

import com.sun.jdi.ObjectReference;

public class ClassValue extends ReferenceValue {
    private JVMField[] fields;

    public ClassValue(String type, long unique_id,
                      ObjectReference objRef, boolean overview) {
        super(type, unique_id, ObjectType.OBJECT);

        if (!overview) {
            fields = null;
        }
    }

    public JVMField[] getFields() {
        return fields;
    }

    public void setFields(JVMField[] fields) {
        this.fields = fields;
    }
}
