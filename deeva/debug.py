from subprocess import Popen, PIPE
from py4j.java_collections import ListConverter
from py4j.java_gateway import JavaGateway, GatewayClient
from py4j.java_gateway import *

from Queue import Queue
import threading

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
    command = ["java", "-classpath", classpath] + javaopts + \
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
        port, proc = launch_gateway(classpath=classpath, die_on_exit=True)
        gateway_client = GatewayClient(port=port) 
        gateway = JavaGateway(gateway_client, auto_convert=True)
        print port, proc, classpath, prog
        #x = gateway.jvm.java.util.ArrayList()
        #x.append(1)
        #print x
        #mytrial = gateway.jvm.MyTrial()#
        #print mytrial.getTrialNo()
        print dir(gateway.jvm.asdf.Debug(prog))
        #debugger = gateway.jvm.deeva.Debug(prog)

        # Start the Response Queue listener
        response_queue_handler = Thread(target=response_queue_method)
        response_queue_handler.daemon = True
        response_queue_handler.start()

        # string_class = gateway.jvm.java.lang.String
        # empty_string_array = gateway.new_array(string_class, 0)

        # debugger.main(empty_string_array)
        #return debugger
        return None

def load(name):
    source = []
    name = name + '.java'
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

class Response_Queue(object):
    def put(string):
        """Add `string' to response queue that will be processed later."""
        response_queue.put(string)

    class Java(object):
        implements = ['deeva.DebugResponseQueue']

def response_queue_method():
    while True:
        debuggee_string = response_queue.get()
        # Put this out to Flask or do stuff with it
        print 'Debuggee output:', debuggee_string
        response_queue.task_done()
