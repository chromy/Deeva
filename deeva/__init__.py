import os
from flask import Flask, jsonify, render_template, request, g, make_response, redirect, url_for, Response
import debug
from debug import load, WrongState
import events
import pprint
from py4j.java_collections import ListConverter
import simplejson as json

app = Flask('deeva')
app.package_dict = {}
app.sources = {}
app.source_code = {}

@app.route("/closeConnection") # take in a con id
def closeCon():
    # Replace with conn id
    from blinker import signal
    sig = signal('deeva')

    sig.send('closeCon', event="close", data="hello")
    return "Close request send"


@app.route("/postData/<event>/<data>")
def postData(event, data):
    print "Post Data:", data
    from blinker import signal
    sig = signal('deeva')
    sig.send('postData', event=event, data={'abc': data})
    return "All Good"

@app.route("/streamHTML")
def stream_html():
    return app.send_static_file('test.html')

@app.route("/stream")
def stream():
    subscriber = debug.DeevaEventSubscriber(["deeva"])



@app.route("/")
def index():
    try:
        return app.send_static_file('index.html')
    except Exception as e:
        print "got something here"

@app.route("/breakPoints")
def breakPoints():
    bkpts = app.debugger.getBreakpoints()
    data = [{'clas':b.getClas(), 'line':b.getLineNumber()} for b in bkpts]
    return jsonify(break_points=data)

@app.route("/stepOver", methods=['POST'])
def step_over():
    return make_api_response(app.debugger.stepOver)

@app.route("/stepInto", methods=['POST'])
def step_into():
    return make_api_response(app.debugger.stepInto)

@app.route("/stepReturn", methods=['POST'])
def step_return():
    return make_api_response(app.debugger.stepReturn)

@app.route("/setBreakpoint", methods=['POST'])
def set_breakpoint():
    breakpoint = request.get_json()
    print breakpoint
    clas = breakpoint['clas']
    line = int(breakpoint['lineNumber'])+1
    result = app.debugger.setBreakpoint(clas, line)
    return jsonify(success=result)

@app.route("/unsetBreakpoint", methods=['POST'])
def unset_breakpoint():
    breakpoint = request.get_json()
    clas = breakpoint['clas']
    line = int(breakpoint['lineNumber'])+1
    result = app.debugger.unsetBreakpoint(clas, line)
    return jsonify(success=result)

@app.route("/run", methods=['POST'])
def run():
    if app.debugger.getStateName() == "NO_INFERIOR":
        request_args = request.get_json()
        argument_array = request_args.get("args")
        enable_assertions = request_args.get("ea")
        java_argument_array = ListConverter().convert(argument_array, app.gateway._gateway_client)

        print 'Starting program...'
        # Aswell as any other arguments e.g. -ea -cp asdf, commandline arguments
        app.debugger.start(app.program, java_argument_array, enable_assertions)
    else:
        print 'Continuing program...'

    return make_api_response(app.debugger.run)

def form_package_dict(sources):
    # Get cached version of the package_dict, we only need to do this once
    if not app.package_dict:

        package_dict = { '(default)': {}, '&sources': {}}
        for source in sources:
            parts = source.split('.')
            packages = parts[:-1]
            className = parts[-1:][0]

            current = package_dict

            if not packages:
                current = package_dict['(default)']

            for package in packages:
                if not current.get(package):
                    current[package] = {'&sources':{}}

                current = current[package]

            if not current.get('&sources'):
                current['&sources'] = {}

            current['&sources'][className] = {
                'className': source,
            }

        # Save the package dict
        app.package_dict = package_dict

    return app.package_dict

def get_sources():
    if not app.sources:
        app.sources = app.debugger.getSources()

    return app.sources

@app.route("/files")
def get_files():
    sources = get_sources()
    package_dir = form_package_dict(sources)

    return jsonify(package_dir=package_dir)

@app.route("/file/<classname>")
def get_code(classname):
    if not app.source_code.get(classname):
        sources = get_sources()
        location = sources.get(classname)

        print "retrieving source code for class:", classname
        print "location:", location

        if not location:
            return jsonify({})

        app.source_code[classname] = load(location)

    all_bkpts = app.debugger.getBreakpoints()
    bkpts = [b.getLineNumber()-1 for b in all_bkpts if b.getClas() == classname]

    response = {'classname' : classname,
                'code' : app.source_code[classname],
                'breakpoints' : bkpts,
                }

    return jsonify(response)

@app.route("/getCurrentState")
def get_state():
    return make_api_response(app.debugger.getState)

@app.route("/pushInput", methods=["POST"])
def push_input():
    args = request.get_json()
    message = args.get('message')

    return make_api_response(app.debugger.putStdInMessage, message)

@app.route("/getHeapObjects", methods=["POST"])
def get_heap_objects():
    args = request.get_json()
    heap_requests = args.get('heap_requests')

    if app.debugger.getStateName() == 'NO_INFERIOR':
        return jsonify(success=False)

    heap_objects = []
    for heap_request in heap_requests:
        unique_id = int(heap_request.get('unique_id'))
        typestr = heap_request.get('type')

        print "< Request for object(id=%d, class=%s) >" % (unique_id, typestr)
        heap_object = app.debugger.getHeapObject(unique_id, typestr)
        if not heap_object:
            continue
        heap_object_json = app.gson_lib.toJson(heap_object)
        heap_object_dict = json.loads(heap_object_json)
        heap_objects.append(heap_object_dict)

    return jsonify(success="true", objects=heap_objects)

@app.route("/ping")
def ping():
    return "success"

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
        stdout, stderr = debug.pop_output()

        # Serialise the Java Object using the gson library and deserialise it
        # into a Python Dictionary
        result_json = app.gson_lib.toJson(result)
        result_dict = json.loads(result_json)

        # If we're awaiting IO, the vm is not a suspended, so just dump stdin
        # and stderr and the state. We can't inspect variables etc.

        # Alternatively if we're just inputting data, when debuggee program not
        # expecting, we similarly dump stdout, stderr and status, as the vm is
        # not in a suspend state.

        if result_dict.get('state') == "AWAITING_IO" or result_dict.get('premature_push'):
            return jsonify(stdout=stdout, stderr=stderr, state=result.get('state'))

        # XXX: fix
        result_dict['line_number'] -= 1

        return jsonify(status='ok', stdout=stdout, stderr=stderr, **result_dict)
