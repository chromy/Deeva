package deeva.processor;

public class JVMValue {
    private final String type;
    private String name;

    public JVMValue(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
