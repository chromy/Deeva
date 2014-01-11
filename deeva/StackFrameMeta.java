package deeva;

import deeva.processor.JVMValue;

import java.util.List;
import java.util.Map;

public class StackFrameMeta {
    private final List<JVMValue> stack;
    private final String methodName;
    private final String className;

    public StackFrameMeta(String methodName, String className,
                          List<JVMValue> stack) {
        this.methodName = methodName;
        this.className = className;
        this.stack = stack;
    }

    public List<JVMValue> getStack() {
        return stack;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassName() {
        return className;
    }
}
