package deeva.processor;

public abstract class ReferenceValue extends JVMValue {
    public static enum ObjectType {
        OBJECT, ARRAY, STRING
    }

    private final long unique_id;
    private final ObjectType object_type;

    public ReferenceValue(String type, long unique_id, ObjectType objectType) {
        super(type);
        this.unique_id = unique_id;
        this.object_type = objectType;
    }

    public long getUniqueId() {
        return unique_id;
    }

    public ObjectType getObjectType() {
        return object_type;
    }
}
