package deeva;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;

import java.util.Map;
import java.util.List;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import deeva.DebugResponseQueue;

public class Debug {
    public enum State {
        NO_INFERIOR,
        STASIS,
        RUNNING,
    }

    private final String[] excludes = {};
    private VirtualMachine vm;
    private StreamRedirectThread outThread;
    private StreamRedirectThread errThread;
    private boolean ready = false;
    private DebugResponseQueue reqQueue;
    private State state;

    int num_steps = 0;

    public Debug(DebugResponseQueue reqQueue) {
        this.reqQueue = reqQueue;
    }

    void printOutArguments(Map<String, Connector.Argument> arguments) {
        /* Find out arguments */
        System.out.println("Arguments");
        for (String arg : arguments.keySet()) {
            System.out.println(arg + ":" + arguments.get(arg));
        }
    }

    VirtualMachine launchTarget(String mainArgs) {
        LaunchingConnector connector = findLaunchingConnector();
        Map<String, Connector.Argument> arguments = connectorArguments(connector, mainArgs);
        System.out.println("launch");
        printOutArguments(arguments);
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

    public void start(String arg) {
        vm = launchTarget(arg);
        EventThread eventThread = new EventThread(vm, excludes);
        eventThread.start();
        redirectOutput();
    }

    public void run() {
        vm.resume();
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
        printOutArguments(arguments);
        Connector.Argument mainArg = (Connector.Argument)arguments.get("main");
        if (mainArg == null) {
            throw new Error("Bad launching connector");
        }

        mainArg.setValue(mainArgs);
        System.out.println("After - con");
        printOutArguments(arguments);
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
