from py4j.java_gateway import JavaGateway, GatewayClient, launch_gateway
import sys

def create_java_debugger(classpath):
        port = launch_gateway(classpath=classpath, die_on_exit=True)
        gateway = JavaGateway(GatewayClient(port=port))
        debugger = gateway.jvm.Debug()
        return debugger

fileName = sys.argv[0]

def load():
    source = []
    try:
        f = open(fileName, "r")
        for line in f:
           source.append(line)
    except IOError:
        print 'cannot open', fileName
    else:
        print fileName, 'has', len(source), 'lines'
        f.close()
    return source
