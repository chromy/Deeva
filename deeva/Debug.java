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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;

import deeva.DebugResponseQueue;
import deeva.WrongStateError;
import deeva.Breakpoint;

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
    private StdInRedirectThread inThread;
    private BlockingQueue<String> inQueue;
    private DebugResponseQueue outQueue;
    private State state;
    private Semaphore sema;
    private List<Map<String, String>> stack;
    private Map<Breakpoint, BreakpointRequest> breakpoints;

    private StepRequest stepRequest;

    int line_number = 0;

    public Debug(DebugResponseQueue outQueue, DebugResponseQueue inQueue) {
        breakpoints = new HashMap<Breakpoint, BreakpointRequest>();
        this.outQueue = outQueue;
	this.inQueue = new LinkedBlockingQueue<String>();
        sema = new Semaphore(0);
        state = State.NO_INFERIOR;
    }

    public void start(String arg) {
        //sema.drainPermits();
        vm = launchTarget(arg);
        EventThread eventThread = new EventThread(vm, excludes, this);
        eventThread.start();
        redirectOutput();
        state = State.STASIS;

        EventRequestManager reqMgr = getRequestManager();
        ClassPrepareRequest prepareRequest = reqMgr.createClassPrepareRequest();
        for (String ex : excludes) { prepareRequest.addClassExclusionFilter (ex); }
        prepareRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);    // suspend so we can examine vars
        prepareRequest.enable();

        attemptToSetWaitingBreakpoints();
    }

    public void putStdInMessage(String msg) throws InterruptedException {
	/* Possibly more validation if necessary */

	/* Pushes given string msg, on to the inQueue that will be fed
	 * into the debuggee stdin */
	this.inQueue.put(msg);
    }

    public List<Map<String, String>> getStack(LocatableEvent event)
        throws IncompatibleThreadStateException, AbsentInformationException,
	       ClassNotLoadedException
    {
	Map<String, String> stack = new HashMap<String, String>();
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
	    Type valueType = variableValue.type();

	    /* Insert local variable meta into a map that will get converted later */
	    varMap.put("name", var.name());
	    varMap.put("type", typeString);

	    if (valueType instanceof IntegerType) {
		System.err.println("Value: " + ((IntegerValue)variableValue).value());
		Integer value = ((IntegerValue)variableValue).value();
		varMap.put("value", value.toString());
	    } else if (valueType instanceof BooleanType) {
		System.err.println("Value: " + ((BooleanValue)variableValue).value());
		Boolean value = new Boolean(((BooleanValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (valueType instanceof ByteType) {
		System.err.println("Value: " + ((ByteValue)variableValue).value());
		Byte value = new Byte(((ByteValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (valueType instanceof CharType) {
		System.err.println("Value: " + ((CharValue)variableValue).value());
		Character value = new Character(((CharValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (valueType instanceof DoubleType) {
		System.err.println("Value: " + ((DoubleValue)variableValue).value());
		Double value = new Double(((DoubleValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (valueType instanceof FloatType) {
		System.err.println("Value: " + ((FloatValue)variableValue).value());
		Float value = new Float(((FloatValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (valueType instanceof LongType) {
		System.err.println("Value: " + ((LongValue)variableValue).value());
		Long value = new Long(((LongValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (valueType instanceof ShortType) {
		System.err.println("Value: " + ((ShortValue)variableValue).value());
		Short value = new Short(((ShortValue)variableValue).value());
		varMap.put("value", value.toString());
	    } else if (valueType instanceof VoidType) {
		System.err.println("Value: void");
		varMap.put("value", "void");
	    }

	    /* Let's deal with Object references */
	    else if (variableValue instanceof ObjectReference) {
		/* This is guaranteed to be unique iff the object hasn't been
		 * disposed of. Not too sure what the implications of this is
		 * for us. */
		Long uniqueID = ((ObjectReference)variableValue).uniqueID();
		System.err.println("UniqueID: " + uniqueID);
		varMap.put("refID", uniqueID.toString());

		if (variableValue instanceof StringReference) {
		    varMap.put("value", ((StringReference)variableValue).value());
		} else if (variableValue instanceof ArrayReference) {
		    Integer length = ((ArrayReference)variableValue).length();
		    varMap.put("length", length.toString());
		    ArrayType arrType = (ArrayType)valueType;
		    System.err.println("length: " + length);
		    System.err.println("component type: " + arrType.componentTypeName());
		}
	    }

	    /* Append the local variable to the end of the list (stack) */
	    localVariables.add(varMap);
	}

	return localVariables;
    }

    public Map<String, Object> run() throws InterruptedException {
        vm.resume();
        state = State.RUNNING;
        sema.acquire();
        return getState();
    }

    public Map<String, Object> getState() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("state", state);
        result.put("line_number", line_number);
        result.put("stack", stack);
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
        step(StepRequest.STEP_OVER);
        sema.acquire();
        return getState();
    }

    public boolean setBreakpoint(String clas, int lineNum) throws AbsentInformationException {
        Breakpoint bkpt = new Breakpoint(clas, lineNum);

        // If the breakpoint exists return true.
        if (breakpoints.keySet().contains(bkpt)) {
            return true;
        }

        try {
            BreakpointRequest req = attemptToSetBreakpoint(clas, lineNum);
            breakpoints.put(bkpt, req);
            return true;
        } catch (NoVMException error) {
            System.err.println("No vm loaded, saving breakpoint for later.");
            breakpoints.put(bkpt, null);
            return true;
        } catch (NoLoadedClassException error) {
            System.err.println("No class loaded, saving breakpoint for later.");
            breakpoints.put(bkpt, null);
            return true;
        } catch (NoLocationException error) {
            // The VM exists and the class was loaded but we can't set a 
            // breakpoint here.
            return false;
        } catch (AbsentInformationException error) {
            System.err.println("Absent Information!");
            // XXX: Handle this case better.
            return false;
        }
    }

    public boolean unsetBreakpoint(String clas, int lineNum) {
        Breakpoint bkpt = new Breakpoint(clas, lineNum);
        if (breakpoints.containsKey(bkpt)) {
            BreakpointRequest req = breakpoints.remove(bkpt);
            if (req != null) {
                EventRequestManager mgr = vm.eventRequestManager();
                mgr.deleteEventRequest(req);
            }
            return true;
        }
        return false;
    }

    public Set<Breakpoint> getBreakpoints() {
        return breakpoints.keySet();
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
    public void classPrepareEvent(ClassPrepareEvent e) {
        attemptToSetWaitingBreakpoints();
        vm.resume();
    }

    private void attemptToSetWaitingBreakpoints() {
        for (Breakpoint b : breakpoints.keySet()) {
            if (breakpoints.get(b) == null) {
                System.err.println("Attempting to set saved breakpoint.");
                try {
                    BreakpointRequest req = attemptToSetBreakpoint(b.getClas(), b.getLineNumber());
                    breakpoints.put(b, req);
                } catch (NoVMException error) {
                    System.err.println("1");
                    // Ignore.
                } catch (NoLoadedClassException error) {
                    System.err.println("2");
                    // Ignore.
                } catch (NoLocationException error) {
                    System.err.println("3");
                    // Ignore this.
                } catch (AbsentInformationException error) {
                    System.err.println("Abstent Information!");
                }
            }
        }
    }

    private BreakpointRequest attemptToSetBreakpoint(String clas, int lineNum) throws
                NoVMException,
                AbsentInformationException,
                NoLoadedClassException,
                NoLocationException
            {
        if (state == State.NO_INFERIOR) { throw new NoVMException(); }

        List<ReferenceType> classes = vm.classesByName(clas);
        if (classes.size() < 1) { throw new NoLoadedClassException(); }
        // XXX: test with emmbeded classes...
        ReferenceType classRef = classes.get(0);

        List<Location> locs = classRef.locationsOfLine(lineNum);
        if (locs.size() < 1) { throw new NoLocationException(); }
        Location loc = locs.get(0);
        EventRequestManager reqMgr = vm.eventRequestManager();

        BreakpointRequest req = reqMgr.createBreakpointRequest(loc);
        //req.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        req.enable();
        return req;
    }

    @Override
    public void stepEvent(StepEvent event)
	throws IncompatibleThreadStateException, AbsentInformationException,
	       ClassNotLoadedException
    {
        System.err.println(event.location().method() + "@" + event.location().lineNumber());
        stack = getStack(event);
        /* Delete the request */
        getRequestManager().deleteEventRequest(event.request());
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

        state = State.STASIS;
        sema.release();
    }

    @Override
    public void exceptionEvent(ExceptionEvent event) {
        System.err.println("EXCEPTION");
        cleanUp();
    }

    @Override
    public void threadDeathEvent(ThreadDeathEvent event) {
        System.err.println("THREAD_DEATH");
        cleanUp();
    }

    @Override
    public void vmDeathEvent(VMDeathEvent event) {
        System.err.println("DEATH");
        cleanUp();
    }

    private EventRequestManager getRequestManager() {
        return vm.eventRequestManager();
    }

    private void cleanUp() {
        for (Breakpoint b : breakpoints.keySet()) {
            breakpoints.put(b, null);
        }

        try {
            errThread.join();
            outThread.join();
        } catch (InterruptedException e) {
            System.err.println("Could not get all output.");
        }
        state = State.NO_INFERIOR;
        sema.release();
    }

    public String getStateName() {
        return state.toString();
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

        errThread = new StreamRedirectThread("stderr",
					     process.getErrorStream(),
					     this.outQueue);

        outThread = new StreamRedirectThread("stdout",
					     process.getInputStream(),
					     this.outQueue);

	inThread = new StdInRedirectThread("stdin",
					   process.getOutputStream(),
					   this.inQueue);

        outThread.start();
        errThread.start();
        inThread.start();
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
}
