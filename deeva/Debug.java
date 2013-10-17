public class Debug {
    int num_steps = 0;

    public int step() {
        return num_steps++;
    }

    public Boolean setBreakPoint() {
        return true;
    }
}
