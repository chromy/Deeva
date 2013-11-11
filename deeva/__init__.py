from flask import Flask, jsonify, render_template, request, g, make_response, redirect, url_for
import debug
from debug import load, WrongState
import os

app = Flask('deeva')

@app.route("/")
def index():
    try: 
        return app.send_static_file('index.html')
    except Exception as e:
        print "got something here"

@app.route("/breakPoints", methods=['POST'])
def breakPoints():
    if request.method == 'POST':
        breakPoints = request.get_json()
        print breakPoints
        for b in breakPoints:
            # XXX: fix line numbers
            app.debugger.setBreakpoint('SimpleLoop', b+1)
        return jsonify(status='ok')

@app.route("/stepOver", methods=['POST'])
def step_over():
    if request.method == 'POST':
        return make_api_response(app.debugger.stepOver)

@app.route("/stepInto", methods=['POST'])
def step_into():
    if request.method == 'POST':
        return make_api_response(app.debugger.stepInto)

@app.route("/stepReturn", methods=['POST'])
def step_return():
    if request.method == 'POST':
        return make_api_response(app.debugger.stepReturn)

@app.route("/setBreakpoint", methods=['POST'])
def set_breakpoint():
    if request.method == 'POST':
        breakpoint = request.get_json()
        print breakpoint
        clas = breakpoint['clas']
        line = int(breakpoint['lineNumber'])+1
        result = app.debugger.setBreakpoint(clas, line)
        return jsonify(success=result)

@app.route("/unsetBreakpoint", methods=['POST'])
def unset_breakpoint():
    if request.method == 'POST':
        breakpoint = request.get_json()
        clas = breakpoint['clas']
        line = int(breakpoint['lineNumber'])+1
        result = app.debugger.unsetBreakpoint(clas, line)
        return jsonify(success=result)

@app.route("/run", methods=['POST'])
def run():
    if request.method == 'POST':
        return make_api_response(app.debugger.run)

@app.route("/main_class.json")
def get_main_class():
    return get_code(app.program)

@app.route("/file/<name>.json")
def get_code(name):
    name = name + '.java'
    code = load(name)
    return jsonify(file_name=name, code=code)

@app.route("/getCurrentState")
def get_state():
    return make_api_response(app.debugger.getState)

@app.errorhandler(500)
def page_not_found(error):
    import traceback
    print 'Error:', error
    print traceback.print_exc()
    return "500"

def make_api_response(f, *args, **kargs):
    try:
        result = f(*args, **kargs)
    except WrongState as e:
        return jsonify(
                status='error',
                error='Invalid call ' + f.__name__ + ' in this state.'
                )
    else:
        stdout = debug.pop_stdout()
        # XXX: fix
        result['line_number'] -= 1
        st = result['stack']

        # Need to do some sort of recursive converter, so that we don't have
        # maliciuos strings in Java that will kill our eval/repr etc
        result2 = {'state' : result['state'], 
                   'line_number' : result['line_number'], 
                   'stack' : eval(repr(result['stack']))}
        return jsonify(status='ok', stdout=stdout, **result2) 
