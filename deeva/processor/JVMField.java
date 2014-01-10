package deeva.processor;

public class JVMField {
    public static enum Accessibility {
        PRIVATE, PROTECTED, PACKAGE_PRIVATE, PUBLIC
    }

    private final String name;
    private final JVMValue value;
    private final Accessibility access;
    private final boolean is_final;
    private final boolean is_static;

    public JVMField(String name, JVMValue value, Accessibility access,
                    boolean is_final, boolean is_static) {
        this.name = name;
        this.value = value;
        this.access = access;
        this.is_final = is_final;
        this.is_static = is_static;
    }
}
