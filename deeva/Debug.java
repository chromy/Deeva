package deeva;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import java.util.Map;
import java.util.List;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import deeva.DebugResponseQueue;

public class Debug implements EventHandler {
    public static enum State {
        NO_INFERIOR,
        STASIS,
        RUNNING,
    }

    private final String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*"};

    private VirtualMachine vm;
    private StreamRedirectThread outThread;
    private StreamRedirectThread errThread;
    private boolean ready = false;
    private DebugResponseQueue reqQueue;
    private State state;

    int num_steps = 0;

    public Debug(DebugResponseQueue reqQueue) {
        System.err.println("Debug");
        this.reqQueue = reqQueue;
        state = State.NO_INFERIOR;
    }

    public void start(String arg) {
        vm = launchTarget(arg);
        EventThread eventThread = new EventThread(vm, excludes, this);
        eventThread.start();
        redirectOutput();
        state = State.STASIS;
    }

    public void run() {
        vm.resume();
        state = State.RUNNING;
    }

    public void stepInto() {
        if (state == State.STASIS) {
            step(StepRequest.STEP_INTO);
        }
    }

    public void stepOut() {
        if (state == State.STASIS) {
            step(StepRequest.STEP_OUT);
        }
    }

    private void step(int depth) {
        EventRequestManager reqMgr = vm.eventRequestManager();
        StepRequest request = reqMgr.createStepRequest(getThread(),
                StepRequest.STEP_LINE, depth);
        request.addCountFilter(1);
        //for (int i=0; i<excludes.length; ++i) {
        //     request.addClassExclusionFilter(excludes[i]);
        //}
        request.addClassFilter("HelloWorld");
        request.enable();
        vm.resume();
    }

    public void handleEvent(Event event) {
        System.err.println("handleEvent");
        System.err.println(event.getClass().getName());
        if (event instanceof ExceptionEvent) {
            //exceptionEvent((ExceptionEvent)event);
        } else if (event instanceof ModificationWatchpointEvent) {
            //fieldWatchEvent((ModificationWatchpointEvent)event);
        } else if (event instanceof MethodEntryEvent) {
            //methodEntryEvent((MethodEntryEvent)event);
        } else if (event instanceof MethodExitEvent) {
            //methodExitEvent((MethodExitEvent)event);
        } else if (event instanceof StepEvent) {
            EventRequestManager mgr = vm.eventRequestManager();
            System.err.println(((StepEvent)event).location().lineNumber());
            //System.err.println(((StepEvent)event).location().sourceName());
            System.err.println(((StepEvent)event).location().method());
            mgr.deleteEventRequest(event.request());
            //stepEvent((StepEvent)event);
        } else if (event instanceof ThreadDeathEvent) {
            //threadDeathEvent((ThreadDeathEvent)event);
        } else if (event instanceof ClassPrepareEvent) {
            //classPrepareEvent((ClassPrepareEvent)event);
        } else if (event instanceof VMStartEvent) {
            //vmStartEvent((VMStartEvent)event);
        } else if (event instanceof VMDeathEvent) {
            //vmDeathEvent((VMDeathEvent)event);
        } else if (event instanceof VMDisconnectEvent) {
            state = State.NO_INFERIOR;
            //vmDisconnectEvent((VMDisconnectEvent)event);
        } else {
            throw new Error("Unexpected event type");
        }

        reqQueue.put("I GOT AN EVENT!");
    }

    public State getState() {
        return state;
    }

    private ThreadReference getThread() {
        for (ThreadReference ref : vm.allThreads()) {
            System.err.println("Thread: " + ref.name());
        }
        // We only handle the main thread.
        for (ThreadReference ref : vm.allThreads()) {
            if (ref.name().equals("main")) {
                return ref;
            }
        }
        assert false : "No main thread.";
        return null;
    }





    // XXX: Refactor beneath this line... and above this line...

    void printOutArguments(Map<String, Connector.Argument> arguments) {
        /* Find out arguments */
        System.err.println("Arguments");
        for (String arg : arguments.keySet()) {
            System.err.println(arg + ":" + arguments.get(arg));
        }
    }

    VirtualMachine launchTarget(String mainArgs) {
        LaunchingConnector connector = findLaunchingConnector();
        Map<String, Connector.Argument> arguments = connectorArguments(connector, mainArgs);
        System.out.println("launch");
        try {
            return connector.launch(arguments);
        } catch (IOException exc) {
            throw new Error("Unable to launch target VM: " + exc);
        } catch (IllegalConnectorArgumentsException exc) {
            throw new Error("Internal error: " + exc);
        } catch (VMStartException exc) {
            throw new Error("Target VM failed to initialize: " + exc.getMessage());
        }
    }

    void redirectOutput() {
        Process process = vm.process();

        // Copy target's output and error to our output and error.
        // errThread = new StreamRedirectThread("error reader",
        //         process.getErrorStream(),
        //         System.err);
        // outThread = new StreamRedirectThread("output reader",
        //         process.getInputStream(),
        //         System.out);
        //errThread.start();
        outThread = new StreamRedirectThread("output reader",
                process.getInputStream(),
                this.reqQueue);

        outThread.start();

        /* Somehow need to capture input i.e. in the other direction */
    }

    LaunchingConnector findLaunchingConnector() {
        List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
        for (Connector connector : connectors) {
            if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
                return (LaunchingConnector)connector;
            }
        }
        throw new Error("No launching connector");
    }

    /**
     * Return the launching connector's arguments.
     */
    Map<String, Connector.Argument> connectorArguments(LaunchingConnector connector, String mainArgs) {
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        System.out.println("Before - con");
        Connector.Argument mainArg = (Connector.Argument)arguments.get("main");
        if (mainArg == null) {
            throw new Error("Bad launching connector");
        }

        mainArg.setValue(mainArgs);

        //((Connector.Argument)(arguments.get("suspend"))).setValue("true");

        System.out.println("After - con");
        return arguments;
    }

    public int step() {
        return num_steps++;
    }

    public Boolean setBreakPoint() {
        return true;
    }

    public static String hello() {
        return "Hello";
    }
}
