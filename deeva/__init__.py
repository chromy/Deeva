from flask import Flask, jsonify, render_template, request
from debug import get_debug, load
app = Flask('deeva')

@app.route("/")
def index():
    return render_template("index.html")

@app.route("/step", methods=['POST'])
def step():
    if request.method == 'POST':
        return jsonify(step_number=get_debug().step())

@app.route("/javacode.json")
def code():
    codeName = "Trial code!"
    code = load()
    return jsonify(codeName=codeName,
                   code=code)

if __name__ == '__main__':
   app.run(host='0.0.0.0')
   print 'hello'
