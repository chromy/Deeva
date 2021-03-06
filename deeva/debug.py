import traceback
from subprocess import Popen, PIPE
from py4j.java_collections import ListConverter
from py4j.protocol import Py4JJavaError
from py4j.java_gateway import JavaGateway, GatewayClient
from py4j.java_gateway import *

from Queue import Queue, Empty
import threading, traceback
from blinker import signal
import events
import logging
import logging.config

class WrongState(Exception):
    pass

class ResponseQueue(object):
    def __init__(self):
        self.response_queue = Queue()
    def put(self, stream, string):
        """Add `string' to response queue that will be processed later."""
        print repr(string)
        self.response_queue.put((stream, string))

    def get(self):
        # This call is blocking
        string = self.response_queue.get(True)
        self.response_queue.task_done()
        return string

    class Java:
        implements = ['deeva.DebugResponseQueue']

# Globals
out_queue = ResponseQueue()
deeva_event_dispatcher = events.DeevaEventDispatcher()

# fix
def launch_gateway(port=0, jarpath="", classpath="", javaopts=[],
                   die_on_exit=False):
    if not jarpath:
        jarpath = find_jar_path()

    # Fail if the jar does not exist.
    if not os.path.exists(jarpath):
        raise Py4JError("Could not find py4j jar at {0}".format(jarpath))

    # Launch the server in a subprocess.
    classpath = os.pathsep.join((jarpath, classpath))
    command = ["java", "-ea",  "-classpath", classpath] + javaopts + \
              ["py4j.GatewayServer"]
    if die_on_exit:
        command.append("--die-on-broken-pipe")
    command.append(str(port))
    logger.debug("Lauching gateway with command {0}".format(command))
    proc = Popen(command, stdout=PIPE, stdin=PIPE)

    # Determine which port the server started on (needed to support
    # ephemeral ports)
    _port = int(proc.stdout.readline())
    return (_port, proc)

def create_java_debugger(classpath, prog, debuggee_classpaths,
                         debuggee_sourcepaths, ea, debuggee_args):
    port, _ = launch_gateway(classpath=classpath, die_on_exit=True)
    gateway_client = GatewayClient(port=port)
    gateway = JavaGateway(gateway_client,
                          auto_convert=True,
                          auto_field=True,
                          start_callback_server=True)

    java_args = ListConverter().convert(debuggee_args, gateway._gateway_client)

    # Create output queue thread listener
    def output_dispatcher():
        while True:
            output_type, string = out_queue.get()
            print "output dispatcher", repr(string)
            output_signal = signal('deeva_main')
            output_signal.send('deeva_java', event_obj=events.DeevaOutputEvent(output_type, string))

    output_thread = Thread(target=output_dispatcher)
    output_thread.daemon = True
    output_thread.start()

    # Create debugger instance
    debugger = JavaProxy(gateway.jvm.deeva.Debug(out_queue, debuggee_classpaths,
                                                 debuggee_sourcepaths, prog, ea,
                                                 java_args, deeva_event_dispatcher))

    sources = debugger.getSources()
    return debugger, gateway

class JavaProxy:
    """Translates py4j Java exceptions into Python exceptions.
    Supports only function calls -- not attributes."""
    def __init__(self, obj):
        self.__obj = obj

    def __getattr__(self, name):
        if name in self.__dict__:
            return self.__dict__[name]
        def _missing(*args, **kargs):
            try:
                return getattr(self.__obj, name)(*args, **kargs)
            except Py4JJavaError as e:
                print e.java_exception
                print traceback.print_exc()
                raise WrongState()

        return _missing

def load(name):
    source = []
    try:
        f = open(name, "r")
        for line in f:
           source.append(line)
    except IOError:
        print 'cannot open', name
    else:
        print name, 'has', len(source), 'lines'
        f.close()
    return source

def pop_output():
    results = []
    while True:
        try:
            results.append(out_queue.response_queue.get(False))
        except Empty:
            break
        else:
            out_queue.response_queue.task_done()

    stdout = ''.join([msg for stream, msg in results if stream == "stdout"])
    stderr = ''.join([msg for stream, msg in results if stream == "stderr"])
    return stdout, stderr

def enable_py4j_logging():
    logger = logging.getLogger("py4j")
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.FileHandler('py4j.log', mode='w'))
