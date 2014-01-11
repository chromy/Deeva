package deeva;

import java.util.LinkedList;
import java.util.List;

public class DeevaStateBuilder {

    private Debug.State state = Debug.State.NO_INFERIOR;
    private int line_number = -1;
    private List<StackFrameMeta> stacks = new LinkedList<StackFrameMeta>();
    private String current_class = "";
    private List<String> arguments = new LinkedList<String>();
    private boolean enable_assertions = false;

    public DeevaState create() {
        return new DeevaState(state, line_number, stacks, current_class,
                              arguments, enable_assertions);
    }

    public Debug.State getState() {
        return state;
    }

    public void setState(Debug.State state) {
        this.state = state;
    }

    public int getLineNumber() {
        return line_number;
    }

    public void setLineNumber(int line_number) {
        this.line_number = line_number;
    }

    public List<StackFrameMeta> getStacks() {
        return stacks;
    }

    public void setStacks(List<StackFrameMeta> stacks) {
        this.stacks = stacks;
    }

    public String getCurrentClass() {
        return current_class;
    }

    public void setCurrentClass(String current_class) {
        this.current_class = current_class;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public boolean isEa() {
        return enable_assertions;
    }

    public void setEa(boolean ea) {
        this.enable_assertions = ea;
    }
}
