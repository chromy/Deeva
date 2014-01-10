import blinker, uuid
from Queue import Queue
#import simplejson as json

DEEVA_DEFAULT_STREAM = ['deeva_main', 'deeva_stack_heap', 'deeva_admin']
DEEVA_STACK_HEAP_STREAM = ['deeva_stack_heap', 'deeva_admin']

class DeevaEventDispatcher(object):
    """This class is responsible for message passing between the Java Side (Debugger
    and Debuggee JVMs) and the Python Side (Flask and our own classes).

    This is so that the threads created on the Java Side i.e. by Py4J and
    threads created by each connection request (by Flask) aren't tightly
    coupled. This eases complications in trying to close the JVM without having
    to find and terminate abrubtly any hanging threads.

    """
    def __init__(self):
        # This signal is for events excluding stack/heap, i.e. data
        # needed after the vm suspends etc
        self.deeva_main = blinker.signal('deeva_main')
        # This signal is for events pertaining to the stack/heap
        self.deeva_stack_heap = blinker.signal('deeva_stack_heap')
        # This signal is for events that are to do with Deeva as a
        # whole, not necessarily from the debugger
        self.deeva_admin = blinker.signal('deeva_admin')

    def exit(self):
        """Send a signal to all connections that Deeva is exiting.

        We send a signal to all who are listening, so they can
        initiate closing down procedures.

        """

        self.deeva_admin.send('deeva_python', event_obj=DeevaExitEvent())

    def awaiting_io_event(self):
        """"""

        self.deeva_main.send('deeva_java', event_obj=DeevaAwaitingIOEvent())

    def error(self):
        # TODO: Implement
        self.deeva_main.send('deeva_java', event_obj=DeevaEvent())

    def suspended_event(self, state_data):
        """This event occurs when the Debuggee JVM is suspended.

        We then send a signal to all listeners (Flask request threads) that the
        JVM has been suspended and with it we send the current state of the
        JVM. This then gets pushed to each connected browser.

        Positional Arguments:

        state_data -- py4j dictionary describing the state of the JVM

        """

        self.deeva_main.send('deeva_java',
                             event_obj=DeevaSuspendEvent(state_data))

    def stack_heap_object_event(self, stack_data, heap_data):
        """This event occurs when the Debuggee JVM has the result for a explicit heap
        object request or updates to the stack or the heap objects we're already
        interested in.

        It then sends the relevant signal to all listeners (Flask request
        threads) who then push that change to each connected browser.

        Positional Arguments:

        stack_data -- a list of containing the stack frames for the last time
                      the Debuggee JVM was suspended

        heap_data -- a list of py4j dictionaries each describing a heap
                     object as denoted in the specification

        """

        self.deeva_stack_heap.send('deeva_java',
                                   event_obj=DeevaStackHeapEvent(stack_data,
                                                                 heap_data))

    def absent_information_event(self, class_name):
        """This event occurs in the exception handler for the JDI
        AbsentInformationException.

        The above exception occurs when `class_name' has been compiled without
        debug information. Hence certain operations won't work. We send an error
        to all connected clients (be it full view or stack view) which should
        tell them the JVM has shutdown and also Deeva.

        Positional Arguments:

        class_name -- the class name that caused the exception i.e. the class compiled
                      without debug symbols

        """

        self.deeva_admin.send('deeva_java',
                              event_obj=DeevaAbsentInformationEvent(class_name))

    class Java:
        implements = ['deeva.DeevaEventDispatcher']

class DeevaEventSubscriber(object):
    """This class is responsible for subscibing to Deeva signals and providing an
    event stream.

    The event stream is an Server-Sent Event (SSE) compatible representation of
    the stream of events.

    """
    def __init__(self, topics):
        """Constructor for the DeevaEventSubscriber

        Positional Arguments:

        topics -- list containing topic names that we wish to subscribe to

        """
        # Give unique ID, uuid is guaranteed to have a very very very small
        # chance of collision, for the scope of our project this is okay
        self.subscriber_id = str(uuid.uuid4())

        # Queue to hold the data from the events
        self.queue = Queue()
        self.topics = topics or []

        # Add subscriber_id as a topic, i.e. we subscribe to this connections
        # events, used for when we're closing this connection
        self.topics.append(self.subscriber_id)

        # Subscribe to all the topics
        for topic in topics:
            topic_listener = blinker.signal(topic)
            topic_listener.connect(self.handler)

    def _disconnect_from_signal(self):
        for topic in self.topics:
            topic_listener = blinker.signal(topic)
            topic_listener.disconnect(self.handler)

    def event_stream(self):
        """Generator that produces a stream of text that conforms to the SSE
        protocol."""

        def _is_close_event(event_obj):
            return event_obj.name == 'close'

        # Send the subscriber id
        yield "event: %s\ndata: %s\n\n" % ('subscriber_id', self.subscriber_id)

        # Keep pulling from the Queue that Deeva is pushing to (via signals - blinker)
        while True:
            # Blocking call
            signal_data = self.queue.get()
            event_obj = signal_data.get('event_obj', DeevaEvent())
            event_msgs = event_obj.format_event()

            self.queue.task_done()

            for msg in event_msgs:
                yield msg

            # If we encounter a close event, we close the connection by stopping
            # the generator. The close event would have already been sent to the
            # client, which should have closed the connection on their end
            # too. Probably a better way to handle i.e. heartbeat/timeout.
            if _is_close_event(event_obj):
                self._disconnect_from_signal()
                break

    def handler(self, sender, **kw):
        """Puts the data sent with the signal onto the queue.

        Catches all signals that this subscriber has subscribed to, and puts
        their data onto the queue.

        Positional arguments:

        sender -- name denoting where the signal came from (unused so far)
        kw     -- keyword arguments passed as arguments in the send function, this
                  contains the data the sender wished to send.

        """
        self.queue.put(kw)

class DeevaEvent(object):
    def __init__(self, name='message'):
        self.name = name

    def format_event(self):
        """Formats the data in this event object to conform to the SSE protocol."""
        return []

class DeevaExitEvent(DeevaEvent):
    def __init__(self):
        super(DeevaExitEvent, self).__init__('close')

    def format_event(self):
        messages = []
        messages.append("event: %s\n" % self.name)
        messages.append("data: close\n\n")
        return messages

class DeevaSuspendedEvent(DeevaEvent):
    def __init__(self, state_data):
        super(DeevaSuspendedEvent, self).__init__('suspended')
        self.state_data = state_data

    def format_event(self):
        messages = []
        messages.append("event: %s\n" % self.name)

        return messages

class DeevaStackHeapEvent(DeevaEvent):
    def __init__(self, stack_data, heap_data):
        super(DeevaStackHeapEvent, self).__init__('stack_heap')
        self.stack_data = stack_data
        self.heap_data = heap_data

    def format_event(self):
        messages = []
        messages.append("event: %s\n" % self.name)

        return messages

class DeevaAbsentInformationEvent(DeevaEvent):
    def __init__(self, class_name):
        super(DeevaAbsentInformationEvent, self).__init__('absent_information')
        self.class_name = class_name

    def format_event(self):
        messages = []
        messages.append("event: %s\n" % self.name)
        messages.append("data: %s\n\n" % self.class_name)
        return messages

class DeevaAwaitingIOEvent(DeevaEvent):
    def __init__(self):
        super(DeevaAwaitingIOEvent, self).__init__('awaiting_io')

    def format_event(self):
        messages = []
        messages.append("event: %s\n" % self.name)
        messages.append("data: awaiting_io\n\n")
        return messages

# TODO: one-off requests: arguments, -ea etc, not generated by VM, VM uses these values, so it's okay
