'use strict';

var deeva = angular.module('deeva', ['deeva.directives', 'deeva.services']);

var INITIAL_PROMPT = '';
var CHS_PER_LINE = 80;
var BACK_CLASS = "CodeMirror-activeline-background";
var PASSIVE_BACK_CLASS = "CodeMirror-passiveline-background";

// Currently is a whole document controller
deeva.controller('SimpleController', ['$scope', '$http', 'FileService', 'MiscService',
function ($scope, $http, FileService, MiscService) {
    /*
     * Initialise scope variables
     */

    $scope.currentLine = -1;
    $scope.breakPoints = new Array();
    $scope.showStdIn = true;
    $scope.showArguments = true;
    $scope.currentPrompt = "";
    $scope.stateToPresent = {"STASIS" : "Program paused",
                             "RUNNING" : "Program running",
                             "NO_INFERIOR" : "Program not running",
                             "AWAITING_IO" : "Awaiting I/O"};
    $scope.currentState = "";
    $scope.breadcrumb = ['(default)', 'Select package or source file']; /* Set to be the default package */
    $scope.package_dir = {};
    $scope.current_class = "";

    // ZZZ: Maybe should be in a directive thing somewhere
    /* Define what states that the given button is allowed to be enabled in */
    $scope.state = {
        runBtn :['STASIS', 'NO_INFERIOR'],
        stopBtn : ['RUNNING', 'AWAITING_IO', 'STASIS'],
        stepOverBtn : ['STASIS'],
        stepIntoBtn : ['STASIS'],
        stepReturnBtn : ['STASIS'],
    };


    FileService.getPackages(function(package_dir) {
        $scope.package_dir = package_dir;
    });

    /* ZZZ: These lines should be handled by directives containing the separate ui elements */
    setUpCodeMirror();
    displayTerminal();
    displayTagit();
    init();

    /* Click handler for the debug buttons */
    $scope.clickButton = function(destination, argument_array) {
        if (destination == "run") {
            $scope.currentState = "RUNNING";
            console.log(argument_array);
        }
        $http.post(destination, {args: argument_array})
            .success(function(data) {
                console.log(data);
                updateState(data);
            })
            .error(function(status) {
                console.error("There is an error on " + destination + "()");
            });
    };

    function updateState(data) {
        if (!data) {
            return;
        }

        /* Set state of buttons */
        $scope.currentState = data.state;

        /* Focus input on terminal if we're awaiting I/O */
        if (data.state == 'AWAITING_IO') {
            $scope.terminal.focus(true);
        }

        /* Update the codemirror instance and the stack/heap visuals */
        if (data.line_number && data.current_class) {
            /* Set the breadcrumb (if need be) */
            var new_breadcrumb = data.current_class.split(".");
            var last_index = new_breadcrumb.length - 1;
            if (new_breadcrumb.slice(0, last_index).length == 0) {
                console.log("We're in a default");
                new_breadcrumb = ['(default)'].concat([new_breadcrumb[last_index]]);
            }

            $scope.breadcrumb = new_breadcrumb;

            /* Remove the line highlights */
            $scope.codeMirror.removeLineClass($scope.currentLine, "background");

            /* Update the current class */
            $scope.current_class = data.current_class;

            /* Update the current code mirror instance */
            $scope.loadFile(data.current_class, function() {

                /* Update cm */
                var prev_line = $scope.currentLine;
                var current_line = data.line_number;

                $scope.currentLine = current_line;

                /*if (prev_line >= 0) {
                    $scope.codeMirror.removeLineClass(prev_line, "background", BACK_CLASS);
                }*/

                if ($scope.currentLine >= 0) {
                    var cls = $scope.currentState == "NO_INFERIOR" ? PASSIVE_BACK_CLASS : BACK_CLASS;
                    $scope.codeMirror.addLineClass($scope.currentLine, 'background', cls);
                }

                $scope.codeMirror.setCursor($scope.currentLine);
            });

            // refactor - plus fix heap!
            /* Update the stack and the heap */
            var stack_heap = {'stack' : data.stack};
            if (data.stack && false) { //ZZZ: Horrible hack, but want to get other things working first
                main(stack_heap);
            }
        }

        if (data.stdout) {
            printToTerminal(data.stdout, false);
        }

        if (data.stderr) {
            printToTerminal(data.stderr, true);
        }
    }

    /* Gets the current state of the debugger */
    function init() {
        $http.get('getCurrentState')
            .success(function(data) {
                updateState(data);
            })
            .error(function(status) {
                console.error("There is an error getting state.");
            });
    }

    /* Initialze codeMirror and display it */
    /* YYY: Should be in its own directive controller thing */
    function setUpCodeMirror() {
        //Initialize codeMirror
        $scope.codeMirror = CodeMirror(document.getElementById('codeInputPane'), {
            mode: 'text/x-java',
            tabSize: 4,
            lineNumbers: true,
            lineWrapping: true,
            firstLineNumber: 0,
            readOnly: "nocursor",
            gutters: ["CodeMirror-linenumbers", "breakpoints"],
        });
        setGutterHandler();
        $scope.cmMaxWidth = (80) * ($scope.codeMirror.defaultCharWidth()) + 74;
    }

    /* Set an event handler when the gutter is clicked
       which updates frontend as well as invokthe the method setBreakPoints */
    function setGutterHandler() {
        $scope.codeMirror.on("gutterClick", function(cm, line) {
            var info = cm.lineInfo(line);
            var class_name = $scope.breadcrumb.join('.');

            if (info.gutterMarkers) {
                tryToUnsetBreakpoint(cm, class_name, line);
            } else {
                tryToSetBreakpoint(cm, class_name, line);
            }
        });
    }

    // Invoke a POST method to backend to send a data about a set of breakpoints.
    function tryToSetBreakpoint(cm, clas, lineNumber) {
        console.log("Trying to set breakpoint: " + clas + "@" + lineNumber);
        $http.post('setBreakpoint', {clas: clas, lineNumber: lineNumber})
            .success(function(data) {
                if (data.success) {
                    FileService.addBreakpoint(clas, lineNumber);
                    /* Add marker */
                    var bp = makeBreakPoint();
                    cm.setGutterMarker(lineNumber, "breakpoints", bp);
                } else {
                    console.error("Could not set breakpoint.");
                }
            })
            .error(function(data) {
                console.error("Setting breakpoint error.");
            });
    }

    function tryToUnsetBreakpoint(cm, clas, lineNumber) {
        console.log("Trying to unset breakpoint: " + clas + "@" + lineNumber);
        $http.post('unsetBreakpoint', {clas: clas, lineNumber: lineNumber})
            .success(function(data) {
                if (data.success) {
                    console.log("Unsetting breakpoint.");
                    FileService.removeBreakpoint(clas, lineNumber);
                    /* Remove marker */
                    cm.setGutterMarker(lineNumber, "breakpoints", null);
                } else {
                    console.error("Could not unset breakpoint.");
                }
            })
            .error(function(data) {
                console.error("Setting breakpoint error.");
            });
    }

    function printToTerminal(output, isErr) {
        if (!output) {
            return;
        }

        var lines = output.split("\n");
        for (var index = 0; index < lines.length; index++) {
            var line = lines[index];
            if (index == lines.length - 1) {
                $scope.currentPrompt += (line);
                $scope.terminal.set_prompt($scope.currentPrompt + INITIAL_PROMPT);
            } else {
                var toPrint = $scope.currentPrompt + line;
                toPrint = isErr ? ("[[;#FF0000;#fff]" + toPrint + "]") : toPrint;
                $scope.terminal.echo(toPrint);
                $scope.currentPrompt = INITIAL_PROMPT;
                $scope.terminal.set_prompt(INITIAL_PROMPT);
            }
        }
    }

    function displayTerminal() {
        $scope.terminal = $('#terminal').terminal(function(input, term) {
            /*This function is called whenever the return button is hit. */
            if (input == "") {
                printToTerminal("\n", false);
            } else {
                sendInput(input);
            }
        }, {
            // Initial setup for terminal
            greetings: "",
            height: 200,
            width: "100%",
            prompt: INITIAL_PROMPT,
        });
    }

    function sendInput(input) {
        console.log(input);
        $scope.terminal.set_prompt(INITIAL_PROMPT);
        $http.post('pushInput', {message:input+'\n'})
            .success(function(data) {
                console.log(data)
                updateState(data);
            })
            .error(function(status) {
                console.error("There is an error sending input " + status);
            });
    }

    function displayTagit() {
        //Use the function below to get all arguments
        $scope.arguments = $("#arguments").tagit({
            allowDuplicates: true,
            placeholderText: "Input argument(s) here"
        });
    }

    $scope.getArgumentArray = function() {
        var java_arguments = $scope.arguments.tagit("assignedTags");
        return java_arguments;
    }

    /* Refactoring of loading file on page, refactor again somewhere */
    $scope.loadFile = function(className, callback) {
        callback = callback || MiscService.nullFunction;
        if (className === "") {
            return;
        }

        /* Get the file, any errors associated with getting the file will be displayed in code*/
        FileService.getFile(className, function(classdata) {
            $scope.codeMirror.swapDoc(classdata.code);

            // Update breakpoints.
            $scope.codeMirror.clearGutter("breakpoints");
            for (var i=0; i<classdata.breakpoints.length; i++) {
                var line = classdata.breakpoints[i];
                var info = $scope.codeMirror.lineInfo(line);
                console.log("Breakpoints" + classdata.breakpoints[i]);
                $scope.codeMirror.setGutterMarker(line, "breakpoints", makeBreakPoint());
            }

            /* Execute the callback so the directive can complete any actions it needs to */
            callback();
        });
    };

    $scope.getObj = function(unique_id, typestring) {
        $http.post('/getHeapObject', {'unique_id': parseInt(unique_id), 'typestring': typestring})
            .success(function(data) {
                console.log(data);
            })
            .error(function() {
                console.error("Error getting object");
            });
    };
}]);

// Return a div that contain a marker for breakpoint.
function makeBreakPoint() {
    var breakPoint = document.createElement("div");
    breakPoint.style.color = "#0000FF";
    breakPoint.innerHTML = "●";
    return breakPoint;
}
