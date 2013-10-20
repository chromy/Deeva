#! /usr/bin/env python

import os, webbrowser
from deeva import app, debug

def main(prog):
    # Start the Java debug server
    classpath = os.path.dirname(os.path.abspath(__file__)) + "/deeva"
    app.debugger = debug.create_java_debugger(classpath)

    # Start Flask
    webbrowser.open('http://localhost:5000')
    app.run()

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("prog", help="the program you want to debug")
    args = parser.parse_args()
    main(args.prog)
