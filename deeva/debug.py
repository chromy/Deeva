import traceback
from subprocess import Popen, PIPE
from py4j.java_collections import ListConverter
from py4j.protocol import Py4JJavaError
from py4j.java_gateway import JavaGateway, GatewayClient
from py4j.java_gateway import *

from Queue import Queue, Empty
import threading, traceback

class WrongState(Exception):
    pass

# FIX - SORRY
response_queue = Queue()

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

def create_java_debugger(classpath, prog):
        print "CLASSPATH", classpath
        port, proc = launch_gateway(classpath=classpath, die_on_exit=True)
        gateway_client = GatewayClient(port=port)
        gateway = JavaGateway(gateway_client, auto_convert=True,
                              auto_field=True,
                              start_callback_server=True)
        print port, proc, classpath, prog

        # Setup Response Queue callback
        response_queue_callback = ResponseQueue()

        # Start the Response Queue listener
        #response_queue_handler = Thread(target=response_queue_method)
        #response_queue_handler.daemon = True
        #response_queue_handler.start()

        #string_class = gateway.jvm.java.lang.String
        #empty_string_array = gateway.new_array(string_class, 0)

        debugger = JavaProxy(gateway.jvm.deeva.Debug(response_queue_callback))
        debugger.start(prog)
        print str(debugger.getState()), "state thing"

        # debugger.main(empty_string_array)
        return debugger

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

def pop_stdout():
    result = ''
    while True:
        try:
            result += response_queue.get(False)
        except Empty:
            break
        else:
            response_queue.task_done()
    return result

class ResponseQueue(object):
    def put(self, string):
        """Add `string' to response queue that will be processed later."""
        print repr(string)
        response_queue.put(string)

    class Java:
        implements = ['deeva.DebugResponseQueue']

def response_queue_method():
    while True:
        pass
        #debuggee_string = response_queue.get()
        # Put this out to Flask or do stuff with it
        #print 'Debuggee output:', debuggee_string
        #response_queue.task_done()
