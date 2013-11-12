package deeva;

import java.io.*;
import deeva.DebugResponseQueue;

class StreamRedirectThread extends Thread {

    private final Reader in;
    //private final Writer out;
    private final DebugResponseQueue resQueue;

    private static final int BUFFER_SIZE = 2048;

    /**
     * Set up for copy.
     * @param name  Name of the thread
     * @param in    Stream to copy from
     * @param out   Stream to copy to
     */
    StreamRedirectThread(String name, InputStream in, DebugResponseQueue resQueue) {
        super(name);
        this.in = new InputStreamReader(in);
    	this.resQueue = resQueue;
        setPriority(Thread.MAX_PRIORITY-1);
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
            }
        } catch(IOException exc) {
            System.err.println("Child I/O Transfer - " + exc);
        }
    }
}

