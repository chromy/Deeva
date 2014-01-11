package deeva;

import java.util.List;

public class DeevaStackState {
    private final List<StackFrameMeta> stacks;

    public DeevaStackState(List<StackFrameMeta> stacks) {
        this.stacks = stacks;
    }

    public List<StackFrameMeta> getStacks() {
        return stacks;
    }
}
