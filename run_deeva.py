#! /usr/bin/env python

import os, webbrowser, subprocess
from deeva import app, debug

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

def config(app):
    app.config['DEBUG'] = bool_arg(os.environ.get('DEEVA_DEBUG', 'False'))
    app.config['OPEN_BROWSER'] = bool_arg(os.environ.get('DEEVA_OPEN_BROWSER', 'True'))
    app.config['TESTING'] = bool_arg(os.environ.get('DEEVA_TESTING', 'False'))

def main(prog, args):
    # Read config vars
    config(app)

    # Start the Java debug server
    deeva_cp = os.path.dirname(os.path.abspath(__file__))
    findjava_script = os.path.join(deeva_cp, 'findjava.sh')
    jdi_cp = subprocess.check_output(findjava_script, shell=True).replace('\n', '')
    gson_cp = "lib/gson-2.2.4.jar"
    classpath = deeva_cp + ":" + jdi_cp + ":" + gson_cp

    print "args:", args.args, type(args.args)
    app.debugger, app.gateway = debug.create_java_debugger(classpath, prog, args.cp,
                                                           args.source_cp, args.ea,
                                                           args.args)

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

    main(args.java_class, args)
