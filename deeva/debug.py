from subprocess import Popen, PIPE
from py4j.java_collections import ListConverter
from py4j.java_gateway import JavaGateway, GatewayClient
from py4j.java_gateway import *

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

def create_java_debugger(classpath):
        port, proc = launch_gateway(classpath=classpath, die_on_exit=True)
        gateway_client = GatewayClient(port=port) 
        gateway = JavaGateway(gateway_client, auto_convert=True)
        #x = gateway.jvm.java.util.ArrayList()
        #x.append(1)
        #print x
        mytrial = gateway.jvm.MyTrial()
        print mytrial.getTrialNo()
        debugger = gateway.jvm.Debug("HelloWorld")

        # string_class = gateway.jvm.java.lang.String
        # empty_string_array = gateway.new_array(string_class, 0)

        # debugger.main(empty_string_array)
        print proc.stdout.readline()
        return debugger

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
