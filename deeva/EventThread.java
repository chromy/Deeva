package deeva;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;

public class EventThread extends Thread {
    private final VirtualMachine vm;     // Running VM
    private final String[] excludes;     // Packages to exclude
    private final EventHandler handler;  // The object to receive each event. 

    private boolean connected = true;  // Connected to VM

    EventThread(VirtualMachine vm, String[] excludes, EventHandler handler) {
        super("event-handler");
        this.vm = vm;
        this.excludes = excludes;
        this.handler = handler;
    }

    @Override
    public void run() {
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                while (it.hasNext()) {
                    handler.handleEvent(it.nextEvent());
                }
                //eventSet.resume();
            } catch (InterruptedException exc) {
                // Ignore
            } catch (VMDisconnectedException discExc) {
                //handleDisconnectedException();
                break;
            }
        }
    }
}


