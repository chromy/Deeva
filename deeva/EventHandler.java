package deeva;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

interface EventHandler {
      void handleEvent(Event event) 
	  throws IncompatibleThreadStateException, AbsentInformationException, 
		 ClassNotLoadedException;
}
