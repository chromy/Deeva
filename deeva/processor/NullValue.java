package deeva.processor;

/**
 * Created by felixdesouza on 16/01/2014.
 */
public class NullValue extends JVMValue {
    private final String value;

    public NullValue(String type) {
        super(type);
        this.value = "null";
    }

    public String getValue() {
        return value;
    }
}
