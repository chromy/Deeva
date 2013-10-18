from py4j.java_gateway import JavaGateway, GatewayClient, launch_gateway

def create_java_debugger(classpath):
        port = launch_gateway(classpath=classpath, die_on_exit=True)
        gateway = JavaGateway(GatewayClient(port=port))
        debugger = gateway.jvm.Debug()
        return debugger

fileName = 'MyTrial.java'

def load():
    source = []
    file = open(fileName, 'r')
    for line in file:
        source.append(line)
    file.close()
    return source

