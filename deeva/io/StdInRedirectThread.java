package deeva.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;

public class StdInRedirectThread extends Thread {
    private final BlockingQueue<String> resQueue;
    private final PrintStream out;

    public StdInRedirectThread(String name, OutputStream os,
                        BlockingQueue<String> resQueue)
    {
        super(name);
        this.out = new PrintStream(os);
        this.resQueue = resQueue;
        setPriority(Thread.MAX_PRIORITY-1);
    }

    @Override
    public void run() {
        /* We want to write to the OutputStream (which is
         * connected to the vm process's stdin */

        /* We only get Strings from the queue, let PrintStream
         * handle buffering etc. */

        while (true) {
            try {
                    /* Get next string off the queue - Blocking */
                String s = resQueue.take();

                    /* Write to the debuggee's stdin */
                out.print(s);

                    /* Force this to be pushed to `stdin' */
                out.flush();
            } catch (InterruptedException e) {
                System.err.println("Blocking Queue Interrupted");
                e.printStackTrace();
            }
        }
    }
}
