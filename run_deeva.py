#! /usr/bin/env python

import os, webbrowser
from deeva import app, debug

def main(prog, open_browser=False):
    # Start the Java debug server
    classpath = os.path.dirname(os.path.abspath(__file__)) + "/deeva"
    app.debugger = debug.create_java_debugger(classpath)

    # Open the browser if desired
    if open_browser:
        webbrowser.open('http://localhost:5000')

    # Start Flask
    app.run()

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("prog", help="the program you want to debug")
    parser.add_argument('-s', '--suppress', dest='open_browser', action='store_false', help="do not open browser window")
    args = parser.parse_args()
    main(args.prog, args.open_browser)

