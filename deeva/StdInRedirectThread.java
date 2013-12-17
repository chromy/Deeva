package deeva;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.BlockingQueue;

class StdInRedirectThread extends Thread {
    private final BlockingQueue<String> resQueue;
    private final OutputStreamWriter out;

    StdInRedirectThread(String name, OutputStream out,
			BlockingQueue<String> resQueue)
    {
	super(name);
	this.out = new OutputStreamWriter(out);
	//this.out = new PrintStream(out);
	this.resQueue = resQueue;
	setPriority(Thread.MAX_PRIORITY-1);
    }

    @Override
    public void run() {
	try {
	    /* We want to write to the OutputStream (which is
	     * connected to the vm process's stdin */
	    /* We only get Strings from the queue, let PrintStream
	     * handle buffering etc. */
	    while (true) {
		String s = resQueue.take();
		System.err.println("STDINREDTHREAD: " + s);
		//this.out.print(s);
		this.out.write('c');
	    }
	} catch (InterruptedException exc) {
	    System.err.println("Child Interrupted Exception - " + exc);
	} catch (IOException e) {

	}
    }
}
