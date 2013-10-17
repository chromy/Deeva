#! /usr/bin/env python

import atexit
import sh
from deeva import app, debug

def main(prog):
    # Start the Java debug server
    # Replace by lanuching connector
    import py4j.java_gateway
    path = py4j.java_gateway.find_jar_path()
    p = sh.Command("java")("-cp", "./deeva:"+path, "DebugEntryPoint", _bg=True)

    # Make sure Java sever dies
    atexit.register(p.kill)

    # Connect to the debug server
    debug.connect_to_server()

    # Start Flask
    app.run()

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("prog", help="the program you want to debug")
    args = parser.parse_args()
    main(args.prog)
