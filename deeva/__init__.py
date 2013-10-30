from flask import Flask, jsonify, render_template, request, g, make_response, redirect, url_for
import debug
from debug import load
import os

app = Flask('deeva')

@app.route("/")
def index():
    return app.send_static_file('index.html')

@app.route("/breakPoints", methods=['POST'])
def breakPoints():
    if request.method == 'POST':
        breakPoints = request.data
        return jsonify(status='ok')

@app.route("/step", methods=['POST'])
def step():
    stdout = debug.pop_stdout()
    print 'GET', stdout
    if request.method == 'POST':
        return jsonify(
            step_number=1,
            stdout=stdout,
            )

@app.route("/run", methods=['POST'])
def run():
    if request.method == 'POST':
        app.debugger.run()
        return jsonify(state="started")

@app.route("/main_class.json")
def get_main_class():
    return get_code(app.program)

@app.route("/file/<name>.json")
def get_code(name):
    name = name + '.java'
    code = load(name)
    return jsonify(file_name=name, code=code)

@app.route("/javacode.json")
def code():
    codeName = "Trial code!"
    code = load()
    return jsonify(codeName=codeName,
                   code=code)

@app.errorhandler(500)
def page_not_found(error):
    print 'Error:', error
    return "500"

if __name__ == '__main__':
   app.run()
