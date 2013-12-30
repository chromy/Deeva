'use strict';

var deeva = angular.module('deeva', ['deeva.directives', 'deeva.services']);

var INITIAL_PROMPT = '';
var CHS_PER_LINE = 80;
var BACK_CLASS = "CodeMirror-activeline-background";

// Currently is a whole document controller
deeva.controller('SimpleController', ['$scope', '$http', 'FileService', function ($scope, $http, FileService) {
    $scope.currentLine = -1;
    //$scope.currentFileName = ""; //YYY: Remove
    $scope.breakPoints = new Array();
    $scope.showStdIn = true;
    $scope.showArguments = true;
    $scope.currentPrompt = "";
    $scope.stateToPresent = {"STASIS" : "Program paused",
                             "RUNNING" : "Program running",
                             "NO_INFERIOR" : "Program ended"};

    $scope.currentState = "";

    /* YYY: Remove */
    /*$scope.state = {"runBtn" : false,
                    "stopBtn" : false,
                    "stepOverBtn" : false,
                    "stepIntoBtn" : false,
                    "stepReturnBtn" : false};*/

    /* YYY: Remove */
    /*$scope.files = {};
    $scope.sourceFiles = {};*/

    $scope.breadcrumb = ['(default)']; /* Set to be the default package */
    $scope.package_dir = {};

    FileService.getPackages(function(package_dir) {
	$scope.package_dir = package_dir;
    });

    $scope.current_class = "";

    init();
    /* These lines should be handled by directives containing the separate ui elements */
    //displayCodeMirror();
    setUpCodeMirror();
    displayTerminal();
    displayTagit();
    //getSourceFiles(); /* Slowly phase out */

    /* Click handler for the debug buttons */
    $scope.clickButton = function(destination) {
        if (destination == "run") {
            //setCurrentState("RUNNING");
	    $scope.currentState = "RUNNING";
        }
        $http.post(destination)
            .success(function(data) {
                console.log(data);
                updateState(data);
            })
            .error(function(status) {
                console.log("There is an error on " + destination + "()");
            });
    };

    function updateState(data) {
        if (!data) {
            return;
        }

	/* Set state of buttons */
        //setCurrentState(data.state);
	$scope.currentState = data.state;

	/* Update the codemirror instance and the stack/heap visuals */
	/* TODO: Return current class we're in python code */
	/* TODO: Need a button in the package directive to allow us to go back to current class */
        if (data.line_number && data.current_class) {
	    /* Set the breadcrumb (if need be) */
	    var new_breadcrumb = data.current_class.split(".");

	    $scope.breadcrumb =
		!angular.equals(new_breadcrumb, $scope.breadcrumb) ? new_breadcrumb : $scope.breadcrumb;

	    /* Update the current class */
	    $scope.current_class = data.current_class;

	    /* Update the current code mirror instance */
	    $scope.loadFileOnPage2(data.current_class);

	    /* Update cm */
	    var prev_line = $scope.currentLine;
	    var current_line = data.line_number;

            $scope.currentLine = current_line;
	    if (prev_line >= 0) {
		$scope.codeMirror.removeLineClass(prevLine, "background", BACK_CLASS);
	    }

	    if (current_line >= 0) {
		$scope.codeMirror.addLineClass(currentLine, 'background', BACK_CLASS);
	    }

            $scope.codeMirror.setCursor($scope.currentLine);
            // refactor - plus fix heap!
	    /* Update the stack and the heap */
            var stack_heap = {'stack' : data.stack,
                              'heap' : []
                             }
            main(stack_heap);
        }

        if (data.stdout) {
            printToTerminal(data.stdout, false);
        }

        if (data.stderr) {
            printToTerminal(data.stderr, true);
        }
    }

    /*$scope.$watch('currentLine', function (currentLine, prevLine) {
        console.log(currentLine, prevLine);
        var BACK_CLASS = "CodeMirror-activeline-background";
        if (prevLine >= 0) {
            $scope.codeMirror.removeLineClass(prevLine, "background", BACK_CLASS);
        }
        if (currentLine >= 0) {
            $scope.codeMirror.addLineClass(currentLine, 'background', BACK_CLASS);
        }
    });*/

    /*$scope.$watch('currentFileName', function (currentFile, prevFile) {
        if (currentFile === "") {
            return;
        }
        console.log("Switching to: " + currentFile);
        /* TODO: get the file from our source thing *
        if ($scope.files[currentFile]) {
            codeMirrorSwapTo(currentFile);
        } else {
            console.error("File " + currentFile + " not loaded.");
            console.error($scope.files);
        }
    }); */

    /* YYY: Phase out *
    function codeMirrorSwapTo(fileName) {
        //* TODO - change this to use the package structure *
        $scope.codeMirror.swapDoc($scope.files[fileName].code);
    } */

   /*function setCurrentState(state) {
        $scope.currentState = state;
        //switchRunToContinue(state);
        //setButtonState(state);
    }*/

    /* ZZZ: Maybe should be in a directive somewhere :S *
    function switchRunToContinue(state) {
        if (state) {
            if (state != "NO_INFERIOR") {
                $('#innerRunBtn').show();
                $('#innerRunBtnText').hide();
            } else {
                $('#innerRunBtn').hide();
                $('#innerRunBtnText').show();
            }
        }
    }*/

    // ZZZ: Maybe should be in a directive thing somewhere


    $scope.state = {
	runBtn :['STASIS', 'NO_INFERIOR'],
	stopBtn : ['RUNNING'],
	stepOverBtn : ['STASIS'],
	stepIntoBtn : ['STASIS'],
	stepReturnBtn : ['STASIS', 'NO_INFERIOR']
    }


    /*function setButtonState(state) {
        if (state) {
            switch (state) {
            case "STASIS" :
                $scope.state.runBtn = true;
                $scope.state.stopBtn = false;
                $scope.state.stepOverBtn = true;
                $scope.state.stepIntoBtn = true;
                $scope.state.stepReturnBtn = true;
                break;
            case "RUNNING" :
                $scope.state.runBtn = false;
                $scope.state.stopBtn = true;
                $scope.state.stepOverBtn = false;
                $scope.state.stepIntoBtn = false;
                $scope.state.stepReturnBtn = false;
                break;
            case "NO_INFERIOR" :
                $scope.state.runBtn = true;
                $scope.state.stopBtn = false;
                $scope.state.stepOverBtn = false;
                $scope.state.stepIntoBtn = false;
                $scope.state.stepReturnBtn = false;
                break;
            default :
                $scope.state.runBtn = false;
                $scope.state.stopBtn = false;
                $scope.state.stepOverBtn = false;
                $scope.state.stepIntoBtn = false;
                $scope.state.stepReturnBtn = false;
                break;
            }
            console.log(state);
        }
    }*/

    /* Gets the current state of the debugger */
    function init() {
        $http.get('getCurrentState')
            .success(function(data) {
                //setCurrentState(data.state);
		console.log(data.state);
		//$scope.currentState = data.state;
		//$scope.current_class = data.current_class;
		updateState(data);
            })
            .error(function(status) {
                console.log("There is an error getting state.");
            });
    }

    // Invoke a GET method to ask for Java code.
    // TODO - need to change this to get data from the package thing
    /*function displayCodeMirror() {
        setUpCodeMirror();

        /*
        $http.get('./main_class.json')
            .success(function(data) {
                if (!data.file_name) {
                    alert("Can not load main class please try again");
                }
                if (!data.code) {
                    data.code = ["There is an error getting main class code"];
                };
                $scope.files[data.file_name] = {"code" : CodeMirror.Doc(data.code.join(''), 'text/x-java')};
                console.log(data.file_name);
                $scope.currentFileName = data.file_name;
            })
            .error(function(status) {
                console.log("There is an error main class");
            });
        *
    } */

    // Given a file name, this function will get a code from backend and stroe it in files
    /*
    function getFile(fileName) {
        if (!fileName) {
            console.log("There is an error getting a file of " + fileName);
        }
        $http.get('./file/' + fileName + '.json')
            .success(function(data) {
                if (!data.file_name) {
                    alert("Can not load file " + fileName);
                }
                if (!data.code) {
                    data.code = ["There is an error getting code"];
                }
                $scope.files[fileName] = {'code':CodeMirror.Doc(data.code.join(''), 'text/x-java')};
            })
            .error(function(status) {
                console.log("There is an error getting file " + fileName);
            });
    }*/

    // Initialze codeMirror and display it
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

    // Set an event handler when the gutter is clicked
    // which update frontend as well ass invoke a method setBreakPoints
    function setGutterHandler() {
        $scope.codeMirror.on("gutterClick", function(cm, line) {
            var info = cm.lineInfo(line);
            // XXX: Horrible hack
            // TODO: Horrible hack may be removed
            //var clas = $scope.currentFileName.substring(0, $scope.currentFileName.indexOf('.'));
	    var class_name = $scope.breadcrumb.join('.');
            if (info.gutterMarkers) {
                tryToUnsetBreakpoint(cm, class_name, line);
            } else {
                tryToSetBreakpoint(cm, class_name, line);
            }
        });
    }

    // Return a div that contain a marker for breakpoint.
    function makeBreakPoint() {
        var breakPoint = document.createElement("div");
        breakPoint.style.color = "#0000FF";
        breakPoint.innerHTML = "●";
        return breakPoint;
    }

    // Invoke a POST method to backend to send a data about a set of breakpoint.
    function tryToSetBreakpoint(cm, clas, lineNumber) {
        console.log("Trying to set breakpoint: " + clas + "@" + lineNumber);
        $http.post('setBreakpoint', {clas: clas, lineNumber: lineNumber})
            .success(function(data) {
                if (data.success) {
                    console.log("Setting breakpoint.");
                    breakPoint = makeBreakPoint();
                    //$scope.breakPoints.push(lineNumber);
                    cm.setGutterMarker(lineNumber, "breakpoints", breakPoint);
                    //These are need to store breakpoint to corresponding file
                    // TODO: needs to represented in package structure thing, or maybe some other way
		    /*
                    if (!$scope.files[clas+".java"].breakpoints) {
                        $scope.files[clas+".java"].breakpoints = new Array();
                    }*
                    var breakPoints = $scope.files[clas+".java"].breakpoints;*/
		    FileService.breakpoints(clas, function(breakpoints) {
			breakpoints.push(lineNumber);
		    });
                } else {
                    console.log("Could not set breakpoint.");
                }
            })
            .error(function(data) {
                console.log("Setting breakpoint error.");
            });
    }

    function tryToUnsetBreakpoint(cm, clas, lineNumber) {
        console.log("Trying to unset breakpoint: " + clas + "@" + lineNumber);
        $http.post('unsetBreakpoint', {clas: clas, lineNumber: lineNumber})
            .success(function(data) {
                if (data.success) {
                    console.log("Unsetting breakpoint.");
                    //$scope.breakPoints.splice($scope.breakPoints.indexOf(lineNumber), 1);
                    cm.setGutterMarker(lineNumber, "breakpoints", null);
                    //These are need to store breakpoint to corresponding file
                    //TODO: Need to work out what to do wtih package thing..
		    /* Do we really want to empty the array? */
                    //$scope.files[clas+".java"].breakpoints = new Array();
                    //var breakPoints = $scope.files[clas+".java"].breakpoints;

		    FileService.breakpoints(clas, function(breakpoints) {
			/* Find and remove a previously set breakpoint */
			var index = breakpoints.indexOf(lineNumber);
			if (index > -1) {
                            breakpoints.splice(index, 1);
			}
		    });
                } else {
                    console.log("Could not unset breakpoint.");
                }
            })
            .error(function(data) {
                console.log("Setting breakpoint error.");
            });
    }

    // Invoke a POST method to backend to send a data about a set of breakpoint.
    /*function setBreakPoints() {
        $http.post('breakPoints', $scope.breakPoints)
            .success(function(data) {
                //console.log(data.status);
            })
            .error(function(data) {
                console.log("There is an error sending break points " + data.status);
            });
    }*/

    function printToTerminal(output, isErr) {
        if (!output) {
            return;
        }
        var lines = output.split("\n");
        for (index = 0;index<lines.length;index++) {
            line = lines[index];
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
            // This function is called whenever the enter is hit.
            if (input == "") {
                printToTerminal("\n", false);
            } else {
                sendInput($scope, input);
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
        $scope.terminal.set_prompt(INITIAL_PROMPT);
        $http.post('input', input)
            .success(function(data) {
            })
            .error(function(data) {
                console.log("There is an error sending input " + data.status);
            });
    }

    function displayTagit() {
        //Use the function below to get all arguments
        //console.log($scope.arguments.tagit("assignedTags"));
        $scope.arguments = $("#arguments").tagit({
            allowDuplicates: true,
            placeholderText: "Input argument(s) here"
        });
    }

    /* YYY: Slowly phase out *
    function getSourceFiles() {
        $http.get('./files')
            .success(function(data) {
                if (!data.files) {
                    console.error("Server did not return files.");
                }

                $scope.sourceFiles = data.files;
                //$scope.package_dir = data.files;
                //var index = $scope.sourceFiles.indexOf(data.file_name);
                //$scope.sourceFiles.splice(index, 1);
                console.log($scope.sourceFiles);
                // XXX: Change to load on damand
                for (i=0; i<data.files.length; i++) {
                    // XXX: hack
                    var file = data.files[i];
                    getFile(file);
                }
                $scope.tabWidth = $scope.cmMaxWidth/Object.keys($scope.sourceFiles).length;
            })
            .error(function(status) {
                console.error("There is an error getting files ");
            });
    } */

    /* YYY: Phase out *
    $scope.loadFileOnPage = function(fileName) {
        $scope.currentFileName = fileName;
        console.log($scope.tabWidth);
    };*/

    /* Refactoring of loading file on page, refactor again somewhere */
    $scope.loadFileOnPage2 = function(className) {
	console.log("Selecting source: ", className);
	if (className === "") {
            return;
        }

	/* Get the file, any errors associated with getting the file will be displayed in code*/
        console.log("Switching to: " + className);

	FileService.getFile(className, function(classdata) {
	    $scope.codeMirror.swapDoc(classdata.code);
	});
    };

    //$scope.loadFileOnPage = function(fileName) {
    //    var tempFile = $scope.currentFileName;
    //    var index = $scope.sourceFiles.indexOf(fileName);
    //    $scope.currentFileName = fileName;
    //    $scope.sourceFiles[index] = tempFile;
    // };
}]);
