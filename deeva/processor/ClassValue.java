package deeva.processor;

import com.sun.jdi.ObjectReference;

import java.util.List;

public class ClassValue extends ReferenceValue {
    private List<JVMField> fields;

    public ClassValue(String type, long unique_id,
                      ObjectReference objRef, boolean overview) {
        super(type, unique_id, ObjectType.OBJECT);

        if (!overview) {
            fields = null;
        }
    }

    public List<JVMField> getFields() {
        return fields;
    }

    public void setFields(List<JVMField> jvmFieldList) {
        this.fields = jvmFieldList;
    }
}
