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

import java.util.concurrent.Semaphore;

import deeva.DebugResponseQueue;

public class Debug extends EventHandlerBase {
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
    private Semaphore sema;

    private StepRequest stepRequest;
    private MethodEntryRequest entryRequest;
    private MethodExitRequest exitRequest;
    int line_number = 0;

    public Debug(DebugResponseQueue reqQueue) {
        this.reqQueue = reqQueue;
        sema = new Semaphore(0);
        state = State.NO_INFERIOR;
    }

    public void start(String arg) {
        vm = launchTarget(arg);
        EventThread eventThread = new EventThread(vm, excludes, this);
        eventThread.start();
        redirectOutput();
        state = State.STASIS;


        EventRequestManager reqMgr = vm.eventRequestManager();

        entryRequest = reqMgr.createMethodEntryRequest();
        for (String ex: excludes) { entryRequest.addClassExclusionFilter (ex); }
        entryRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);    // suspend so we can examine vars
        entryRequest.enable();

        exitRequest = reqMgr.createMethodExitRequest();
        for (String ex: excludes) { exitRequest.addClassExclusionFilter (ex); }
        exitRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);    // suspend so we can examine vars
        //exitRequest.enable();

    }

    public void run() {
        vm.resume();
        state = State.RUNNING;
    }

    public int stepInto() throws InterruptedException {
        entryRequest.disable();
        exitRequest.enable();
        step(StepRequest.STEP_INTO);
        sema.acquire();
        exitRequest.disable();
        entryRequest.enable();
        return line_number;
        //if (state == State.STASIS) {
        //}
    }

    public int stepOut() throws InterruptedException {
        entryRequest.disable();
        step(StepRequest.STEP_OUT);
        sema.acquire();
        entryRequest.enable();
        return line_number;
        //if (state == State.STASIS) {
        //}
    }

    public int stepOver() throws InterruptedException {
        entryRequest.disable();
        step(StepRequest.STEP_OVER);
        sema.acquire();
        entryRequest.enable();
        return line_number;
        //if (state == State.STASIS) {
        //}
    }

    public void setBreakPoint(String clas, int lineNum) {
         List<ReferenceType> classes = vm.allClasses();
         for (ReferenceType ref : classes) {
             System.err.println(ref.name());
         }
    }

    private void step(int depth) {
        EventRequestManager reqMgr = vm.eventRequestManager();
        stepRequest = reqMgr.createStepRequest(getThread(),
                StepRequest.STEP_LINE, depth);
        stepRequest.addCountFilter(1);
        for (int i=0; i<excludes.length; ++i) {
             stepRequest.addClassExclusionFilter(excludes[i]);
        }
        stepRequest.enable();
        vm.resume();
    }

    @Override
    public void methodEntryEvent(MethodEntryEvent event) {
        final Method method = event.method();
        System.err.println(method.toString());
        // XXX: hack
        if (!method.toString().equals("SimpleLoop.main(java.lang.String[])")) {
            vm.resume();
        }
    }

    @Override
    public void stepEvent(StepEvent event) {
        System.err.println(event.location().method() + "@" + event.location().lineNumber());
        line_number = event.location().lineNumber();
        EventRequestManager mgr = vm.eventRequestManager();
        mgr.deleteEventRequest(event.request());
        sema.release();
    }

    @Override
    public void methodExitEvent(MethodExitEvent event) {
        if (stepRequest != null) {
            sema.release();
            EventRequestManager mgr = vm.eventRequestManager();
            mgr.deleteEventRequest(stepRequest);
        }
    }

    public State getState() {
        return state;
    }

    private ThreadReference getThread() {
        // We only handle the main thread.
        for (ThreadReference ref : vm.allThreads()) {
            if (ref.name().equals("main")) {
                return ref;
            }
        }
        throw new Error("No main thread.");
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

    public Boolean setBreakPoint() {
        return true;
    }

    public static String hello() {
        return "Hello";
    }
}
