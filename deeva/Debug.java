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
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import deeva.DebugResponseQueue;
import deeva.WrongStateError;

public class Debug extends EventHandlerBase {
    public static enum State {
        NO_INFERIOR,
        STASIS,
        RUNNING;

        public String __html__() {
            return this.toString();
        }
    }

    private final String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*"};

    private VirtualMachine vm;
    private StreamRedirectThread outThread;
    private StreamRedirectThread errThread;
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
        state = State.NO_INFERIOR;

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

    public Map<String, Object> run() throws InterruptedException {
        vm.resume();
        state = State.RUNNING;
        sema.acquire();
        state = State.STASIS;
        return getState();
    }

    public Map<String, Object> getState() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("state", state);
        result.put("line_number", line_number);
        return result;
    }

    public Map<String, Object> stepInto() throws InterruptedException {
        if (state != State.STASIS) {
            throw new WrongStateError("Should be in STASIS state.");
        }
        step(StepRequest.STEP_INTO);
        sema.acquire();
        return getState();
    }

    public Map<String, Object> stepReturn() throws InterruptedException {
        if (state != State.STASIS) {
            throw new WrongStateError("Should be in STASIS state.");
        }
        step(StepRequest.STEP_OUT);
        sema.acquire();
        return getState();
    }

    public Map<String, Object> stepOver() throws InterruptedException {
        if (state != State.STASIS) {
            throw new WrongStateError("Should be in STASIS state.");
        }
        entryRequest.disable();
        step(StepRequest.STEP_OVER);
        sema.acquire();
        return getState();
    }

    public void setBreakPoint(String clas, int lineNum) throws AbsentInformationException {
        ReferenceType classRef = vm.classesByName(clas).get(0);
        Location loc = classRef.locationsOfLine(lineNum).get(0);
        EventRequestManager reqMgr = vm.eventRequestManager();
        BreakpointRequest req = reqMgr.createBreakpointRequest(loc);
        req.enable();
    }

    private void step(int depth) {
        EventRequestManager reqMgr = vm.eventRequestManager();
        stepRequest = reqMgr.createStepRequest(getThread(),
                StepRequest.STEP_LINE, depth);
        for (int i=0; i<excludes.length; ++i) {
             stepRequest.addClassExclusionFilter(excludes[i]);
        }
        //stepRequest.addCountFilter(1);
        stepRequest.enable();
        vm.resume();
    }

    @Override
    public void handleEvent(Event e) 
	throws IncompatibleThreadStateException, AbsentInformationException, 
	       ClassNotLoadedException 
    {
        System.err.println(e.getClass());
        if (e instanceof LocatableEvent) {
            locatableEvent((LocatableEvent)e);
        }
        super.handleEvent(e);
    }

    public void locatableEvent(LocatableEvent e) {
        line_number = e.location().lineNumber();
    }

    @Override
    public void methodEntryEvent(MethodEntryEvent event) {
        final Method method = event.method();
        System.err.println(method.toString());
        // XXX: hack
        if (!method.toString().equals("SimpleLoop.main(java.lang.String[])")) {
            vm.resume();
        } else {
            state = State.STASIS;
            sema.release();
        }
    }

    @Override
    public void stepEvent(StepEvent event) 
	throws IncompatibleThreadStateException, AbsentInformationException, 
	       ClassNotLoadedException
    {
        System.err.println(event.location().method() + "@" + event.location().lineNumber());
	/* Try to extract stack variables - Hack */
	/* Get the thread in which we're stepping */
	ThreadReference threadRef = event.thread();

	/* Get the top most strack frame in the thread that we've stopped in */
	StackFrame stackFrame = threadRef.frame(0);

	/* We want to create a list of maps */
	List<Map<String, String>> localVariables = new LinkedList<Map<String, String>>();	

	/* List all the variables on the stack */
	for (LocalVariable var : stackFrame.visibleVariables()) {
	    Map<String, String> varMap = new HashMap<String, String>();	   
	    
	    String name = var.name();
	    Type type = var.type();
	    String typeString = var.typeName();
	    Value variableValue = stackFrame.getValue(var); 
	    /*System.err.println("Type string: " + typeString);
	    System.err.println("Type instance: " + type.getClass().getName());*/
	    System.err.println("-------------");
	    System.err.println("Name: " + name);
	    System.err.println("Type: " + typeString);
	    System.err.println("ValueType: " + variableValue.type());

	    /* Insert local variable meta into a map that will get converted later */
	    varMap.put("name", var.name());
	    varMap.put("type", typeString);

	    if (variableValue instanceof IntegerValue) {		
		System.err.println("Value: " + ((IntegerValue)variableValue).value());
		Integer value = ((IntegerValue)variableValue).value();
		varMap.put("value", value.toString());
	    } else if (variableValue instanceof BooleanValue) { 
		System.err.println("Value: " + ((BooleanValue)variableValue).value());
		Boolean value = new Boolean(((BooleanValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (variableValue instanceof ByteValue) {
		System.err.println("Value: " + ((ByteValue)variableValue).value());
		Byte value = new Byte(((ByteValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (variableValue instanceof CharValue) {
		System.err.println("Value: " + ((CharValue)variableValue).value());
		Character value = new Character(((CharValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (variableValue instanceof DoubleValue) {
		System.err.println("Value: " + ((DoubleValue)variableValue).value());
		Double value = new Double(((DoubleValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (variableValue instanceof FloatValue) {
		System.err.println("Value: " + ((FloatValue)variableValue).value());
		Float value = new Float(((FloatValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (variableValue instanceof LongValue) {
		System.err.println("Value: " + ((LongValue)variableValue).value());
		Long value = new Long(((LongValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (variableValue instanceof ShortValue) {
		System.err.println("Value: " + ((ShortValue)variableValue).value());
		Short value = new Short(((ShortValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (variableValue instanceof VoidValue) {
		System.err.println("Value: void");		
		varMap.put("value", "void");
	    } 
	    
	    /* Let's deal with Reference Types */
	    /* We need to enumerate the array/objects, and then convert
	     * all the Value objects to some representable format,
	     * we also need to make sure that we don't have any
	     * loops */
	    else if (variableValue instanceof StringReference) {
		System.err.println("Value: \"" + ((StringReference)variableValue).value() + "\"");
		varMap.put("value", ((StringReference)variableValue).value());
	    } else if (variableValue instanceof ArrayReference) {
		System.err.println("Array of length: " + ((ArrayReference)variableValue).length());
		Integer length = ((ArrayReference)variableValue).length();
		varMap.put("length", length.toString());	       
	    } else if (variableValue instanceof ObjectReference) {
		
	    }
	       
	    /* Append the local variable to the end of the list (stack) */
	    localVariables.add(varMap);
	}
       
	System.err.println("-------------");

	/* Delete the request */
        EventRequestManager mgr = vm.eventRequestManager();
        mgr.deleteEventRequest(event.request());
        sema.release();
    }

    @Override
    public void breakpointEvent(BreakpointEvent event) {
        System.err.println(event.location().method() + "@" + event.location().lineNumber());
        // XXX: What to do on step?
        //if (stepRequest != null) {
        //    EventRequestManager mgr = vm.eventRequestManager();
        //    mgr.deleteEventRequest(stepRequest);
        //    stepRequest = null;
        //}
	
	/* Try to extract the stack variables */
	


        sema.release();
    }

    @Override
    public void methodExitEvent(MethodExitEvent event) {
        System.err.println("METHOD EXIT");
        //if (stepRequest != null) {
        //    sema.release();
        //    EventRequestManager mgr = vm.eventRequestManager();
        //    mgr.deleteEventRequest(stepRequest);
        //    stepRequest = null;
        //}
    }

    public void exceptionEvent(ExceptionEvent event) {
        System.err.println("EXCEPTION");
    }

    public void vmDeathEvent(VMDeathEvent event) {
        System.err.println("DEATH");
        sema.release();
    }

    public State getStateName() {
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

        errThread = new StreamRedirectThread("error reader",
                process.getErrorStream(),
                this.reqQueue);

        outThread = new StreamRedirectThread("output reader",
                process.getInputStream(),
                this.reqQueue);

        outThread.start();
        errThread.start();

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
