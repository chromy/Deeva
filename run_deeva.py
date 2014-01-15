#! /usr/bin/env python

import os, webbrowser, subprocess
import deeva
from deeva import app, debug, file_examiner, events
import signal, sys

class ConfigError(Exception):
    pass

def bool_arg(arg):
    arg = arg.lower()
    if arg == 'false':
        return False
    elif arg == 'true':
        return True
    else:
        raise ConfigError()

def ask(question):
    print question, '(y/n)'
    while True:
        a = raw_input('').lower()
        if a == 'y':
            return True
        elif a == 'n':
            return False
        print question, '(y/n)'

def config(app):
    app.config['DEBUG'] = bool_arg(os.environ.get('DEEVA_DEBUG', 'False'))
    app.config['OPEN_BROWSER'] = bool_arg(os.environ.get('DEEVA_OPEN_BROWSER', 'True'))
    app.config['TESTING'] = bool_arg(os.environ.get('DEEVA_TESTING', 'False'))

def main(prog, args):
    # Read config vars
    config(app)

    missing = file_examiner.classes_that_look_out_of_date(args.source_cp)
    if missing != []:
        print 'These files have classes that look like they need to recompiled:'
        print '\n'.join(missing)
        if not ask('Continue?'):
            exit()

    # Start the Java debug server
    deeva_cp = os.path.dirname(os.path.abspath(__file__))
    findjava_script = os.path.join(deeva_cp, 'findjava.sh')
    jdi_cp = subprocess.check_output(findjava_script, shell=True).replace('\n', '')
    gson_cp = os.path.join(deeva.__path__[0], '..', "lib/gson-2.2.4.jar")
    classpath = deeva_cp + ":" + jdi_cp + ":" + gson_cp

    app.debugger, app.gateway = debug.create_java_debugger(classpath, prog, args.cp,
                                                           args.source_cp, args.ea,
                                                           args.args)
    app.gson_lib = app.gateway.jvm.com.google.gson.GsonBuilder().setPrettyPrinting().create()

    # Save the program name
    app.program = prog

    # Open the browser if desired
    if app.config['OPEN_BROWSER']:
        webbrowser.open('http://localhost:5000')

    # Start Flask
    app.run('0.0.0.0', threaded=True)

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("-ea", help="Enable Assertions", action='store_true')
    parser.add_argument("-cp", help="Class path string for ", default=".")
    # Add here small subset of java commands we wish to pass on..
    parser.add_argument("--source_cp",
                        help="Path to the source files, in classpath format, default is the current directory",
                        default=".")
    parser.add_argument("java_class", help="Path to the java class you want to debug")
    parser.add_argument("args", nargs="*", help="Command line arguments for the debuggee program");
    # TODO: Look properly into how Java expects/uses it's arguments
    args = parser.parse_args()

    def signal_handler(signal, frame):
        print 'You pressed Ctrl+C!'
        dispatcher = events.DeevaEventDispatcher()
        dispatcher.exit()
        sys.exit(0)
    signal.signal(signal.SIGINT, signal_handler)

    main(args.java_class, args)
