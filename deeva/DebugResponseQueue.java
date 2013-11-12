package deeva;

public interface DebugResponseQueue {
    public void put(String stream, String response);
    public Object get();
}
