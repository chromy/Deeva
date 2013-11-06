from flask import Flask, jsonify, render_template, request, g, make_response, redirect, url_for
import debug
from debug import load, WrongState
import os

app = Flask('deeva')

@app.route("/")
def index():
    return app.send_static_file('index.html')

@app.route("/breakPoints", methods=['POST'])
def breakPoints():
    if request.method == 'POST':
        breakPoints = request.get_json()
        print breakPoints
        for b in breakPoints:
            # XXX: fix line numbers
            app.debugger.setBreakPoint('SimpleLoop', b+1)
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

@app.route("/setBreakPoint", methods=['POST'])
def breakPoint():
    if request.method == 'POST':
        app.debugger.setBreakPoint('hello', 12)
        return jsonify(status=True)

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
    print 'Error:', error
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
        return jsonify(status='ok', stdout=stdout, **result) 
