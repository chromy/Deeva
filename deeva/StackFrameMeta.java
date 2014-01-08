package deeva;

import java.util.List;
import java.util.Map;

public class StackFrameMeta {
    private final List<Map<String, String>> stackMap;
    private final String methodName;
    private final String className;

    public StackFrameMeta(String methodName, String className,
                          List<Map<String, String>> stackMap) {
        this.methodName = methodName;
        this.className = className;
        this.stackMap = stackMap;
    }

    public List<Map<String, String>> getStackMap() {
        return stackMap;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassName() {
        return className;
    }
}
