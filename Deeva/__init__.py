from flask import Flask, json, jsonify, render_template
import trial
app = Flask('deeva')

@app.route("/")
def index():
    return render_template("index.html")

@app.route("/javacode.json")
def code():
    codeName = "Trial code!"
    code = trial.load()
    return jsonify(codeName=codeName,
                   code=code)

if __name__ == '__main__':
   app.run(host='0.0.0.0') 
   print 'hello'
