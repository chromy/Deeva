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

def main(prog):
    # Read config vars
    config(app)

    # Start the Java debug server
    deeva_cp = os.path.dirname(os.path.abspath(__file__))
    findjava_script = os.path.join(deeva_cp, 'findjava.sh')
    jdi_cp = subprocess.check_output(findjava_script, shell=True).replace('\n', '')
    classpath = deeva_cp + ":" + jdi_cp

    print deeva_cp, jdi_cp, classpath
    app.debugger = debug.create_java_debugger(classpath, prog)

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
    parser.add_argument("prog", help="the program you want to debug")
    args = parser.parse_args()
    main(args.prog)

