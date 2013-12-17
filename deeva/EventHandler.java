package deeva;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.event.Event;

interface EventHandler {
      void handleEvent(Event event) 
	  throws IncompatibleThreadStateException, AbsentInformationException, 
		 ClassNotLoadedException;
}
