import com.sun.jdi.*;
import com.sun.jdi.connect.*;

import java.util.Map;
import java.util.List;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Debug {
    private final VirtualMachine vm;
    private final String[] excludes = {};
    private StreamRedirectThread outThread;
    private StreamRedirectThread errThread;

    int num_steps = 0;

    public Debug(String arg) {
        vm = launchTarget(arg);
        redirectOutput();
        start();
    }

    VirtualMachine launchTarget(String mainArgs) {
        LaunchingConnector connector = findLaunchingConnector();
        Map<String, Connector.Argument> arguments = connectorArguments(connector, mainArgs);

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
        errThread = new StreamRedirectThread("error reader",
                process.getErrorStream(),
                System.err);
        outThread = new StreamRedirectThread("output reader",
                process.getInputStream(),
                System.out);
        errThread.start();
        outThread.start();
    }

    void start() {
        EventThread eventThread = new EventThread(vm, excludes);
        eventThread.start();
        vm.resume();

    //    vm.setDebugTraceMode(debugTraceMode);
    //    eventThread.setEventRequests(watchFields);
    //    redirectOutput();

    //    // Shutdown begins when event thread terminates
    //    try {
    //        eventThread.join();
    //        errThread.join(); // Make sure output is forwarded
    //        outThread.join(); // before we exit
    //    } catch (InterruptedException exc) {
    //        // we don't interrupt
    //    }
    //    writer.close();
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
        Connector.Argument mainArg = (Connector.Argument)arguments.get("main");
        if (mainArg == null) {
            throw new Error("Bad launching connector");
        }

        mainArg.setValue(mainArgs);

        return arguments;
    }

    public int step() {
        return num_steps++;
    }

    public Boolean setBreakPoint() {
        return true;
    }
}
