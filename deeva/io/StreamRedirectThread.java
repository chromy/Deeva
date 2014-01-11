package deeva.io;

import deeva.DebugResponseQueue;
import deeva.DeevaEventDispatcher;
import deeva.OutputDispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StreamRedirectThread extends Thread {

    private static final int BUFFER_SIZE = 2048;
    private final Reader in;
    private final DebugResponseQueue resQueue;
    private final OutputDispatcher dispatcher;

    /**
     * Set up for copy.
     *
     * @param name     Name of the thread
     * @param in       Stream to copy from
     * @param resQueue Output queue that we're pushing stdout/err to.
     */
    public StreamRedirectThread(String name, InputStream in,
                                DebugResponseQueue resQueue,
                                OutputDispatcher dispatcher) {
        super(name);
        this.dispatcher = dispatcher;
        this.in = new InputStreamReader(in);
        this.resQueue = resQueue;
        setPriority(Thread.MAX_PRIORITY - 1);
    }

    /**
     * Copy.
     */
    @Override
    public void run() {
        try {
            char[] cbuf = new char[BUFFER_SIZE];
            int count;
            while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
                String s = new String(cbuf, 0, count);
                resQueue.put(getName(), s);
                dispatcher.dispatchOutput(s);
            }
        } catch (IOException exc) {
            System.err.println("Child I/O Transfer - " + exc);
        }
    }
}

