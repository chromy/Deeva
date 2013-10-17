import time
from py4j.java_gateway import JavaGateway
from py4j.protocol import Py4JNetworkError

debug = None

def _connect_debug(gateway):
    try:
        return gateway.entry_point.getDebugger()
    except Py4JNetworkError:
        return None

def connect_to_server():
    global debug
    gateway = JavaGateway()
    debug = _connect_debug(gateway)
    while debug is None:
        # Add timeout
        debug = _connect_debug(gateway)

def get_debug():
    return debug

filename = 'MyTrial.java'

def load():
    source = []
    file = open(fileName, 'r')
    for line in file:
        source.append(line)
    file.close()
    return source

