package deeva.exception;

public class WrongStateError extends RuntimeException {
    public WrongStateError() { super(); }
    public WrongStateError(String message) { super(message); }
    public WrongStateError(String message, Throwable cause) { super(message, cause); }
    public WrongStateError(Throwable cause) { super(cause); }
}
