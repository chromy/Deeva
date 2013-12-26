package deeva;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.event.*;

public class EventHandlerBase implements EventHandler {

    public void handleEvent(Event event)
            throws IncompatibleThreadStateException, AbsentInformationException,
            ClassNotLoadedException
    {
        if (event instanceof ExceptionEvent) {
            exceptionEvent((ExceptionEvent)event);
        } else if (event instanceof ModificationWatchpointEvent) {
            fieldWatchEvent((ModificationWatchpointEvent)event);
        } else if (event instanceof MethodEntryEvent) {
            methodEntryEvent((MethodEntryEvent)event);
        } else if (event instanceof MethodExitEvent) {
            methodExitEvent((MethodExitEvent)event);
        } else if (event instanceof StepEvent) {
            stepEvent((StepEvent)event);
        } else if (event instanceof BreakpointEvent) {
            breakpointEvent((BreakpointEvent)event);
        } else if (event instanceof ThreadDeathEvent) {
            threadDeathEvent((ThreadDeathEvent)event);
        } else if (event instanceof ClassPrepareEvent) {
            classPrepareEvent((ClassPrepareEvent)event);
        } else if (event instanceof VMStartEvent) {
            vmStartEvent((VMStartEvent)event);
        } else if (event instanceof VMDeathEvent) {
            vmDeathEvent((VMDeathEvent)event);
        } else if (event instanceof VMDisconnectEvent) {
            vmDisconnectEvent((VMDisconnectEvent)event);

        } else {
            throw new Error("Unexpected event type");
        }
    }

    public void exceptionEvent(ExceptionEvent event) {}
    public void fieldWatchEvent(ModificationWatchpointEvent event) {}
    public void methodEntryEvent(MethodEntryEvent event) {}
    public void methodExitEvent(MethodExitEvent event) {}
    public void stepEvent(StepEvent event) throws
            IncompatibleThreadStateException, AbsentInformationException,
            ClassNotLoadedException {}
    public void breakpointEvent(BreakpointEvent event) throws ClassNotLoadedException, AbsentInformationException, IncompatibleThreadStateException {
        // when we get a breakpoint event what does one do?
        System.out.println("We have a breakpoint.");
    }
    public void threadDeathEvent(ThreadDeathEvent event) {}
    public void classPrepareEvent(ClassPrepareEvent event) {}
    public void vmStartEvent(VMStartEvent event) {}
    public void vmDeathEvent(VMDeathEvent event) {}
    public void vmDisconnectEvent(VMDisconnectEvent event) {}
}

