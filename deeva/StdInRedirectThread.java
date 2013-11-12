package deeva;

import java.io.*;
import deeva.DebugResponseQueue;

class StdInRedirectThread extends Thread {
    private final DebugResponseQueue resQueue;
    private final PrintStream out;

    StdInRedirectThread(String name, OutputStream out,
			DebugResponseQueue resQueue)
    {
	super(name);
	this.out = new PrintStream(out);
	this.resQueue = resQueue;
	setPriority(Thread.MAX_PRIORITY-1);
    }

    @Override
    public void run() {
	//try {
	    /* We want to write to the OutputStream (which is
	     * connected to the vm process's stdin */
	    /* We only get Strings from the queue, let PrintStream
	     * handle buffering etc. */
	System.err.println("This is a test.");
	    while (true) {
		String s = (String)resQueue.get();
		System.err.println("STDIN: " + s);
		this.out.print(s);
	    }
	    /*} catch (IOException exc) {
	    System.err.println("Child I/O Transfer - " + exc);
	    }*/
    }
}
