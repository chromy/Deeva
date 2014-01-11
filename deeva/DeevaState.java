package deeva;

import java.util.List;

public class DeevaState {
    private final Debug.State state;
    private final int line_number;
    private final String current_class;
    private final List<String> arguments;
    private final boolean enable_assertions;
    private final List<StackFrameMeta> stacks;
    public boolean premature_push; /* TODO: will be redundant when using
                                       events */

    public DeevaState(Debug.State state, int line_number,
                      List<StackFrameMeta> stacks, String current_class,
                      List<String> arguments, boolean ea) {

        this.state = state;
        this.line_number = line_number;
        this.current_class = current_class;
        this.arguments = arguments;
        this.enable_assertions = ea;
        this.stacks = stacks;
    }

    public Debug.State getState() {
        return state;
    }

    public int getLine_number() {
        return line_number;
    }

    public String getCurrent_class() {
        return current_class;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public boolean isEnable_assertions() {
        return enable_assertions;
    }


    public List<StackFrameMeta> getStacks() {
        return stacks;
    }
}
