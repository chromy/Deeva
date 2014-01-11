package deeva;

import java.util.List;
import java.util.Map;

public interface DeevaEventDispatcher {
    public void exit();
    public void awaiting_io_event();
    public void suspended_event(DeevaState state);
    public void stack_heap_object_event(List<StackFrameMeta> stacks,
                                        List<Map<String, ? extends Object>>
                                            heapObjects);
    public void absent_information_event(String className);
    public void error(String errorName, String errorMessage);
    public void stdout(String s);
    public void stderr(String s);
}
