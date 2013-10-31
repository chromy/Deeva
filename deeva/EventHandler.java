package deeva;

import com.sun.jdi.event.*;

interface EventHandler {
      void handleEvent(Event event);
}
