var deeva = angular.module('deeva', []);

var INITIAL_PROMPT = '';
var CHS_PER_LINE = 80;

// Currently is a whole document controller
deeva.controller('SimpleController', function ($scope, $http) {
  $scope.currentLine = -1;
  $scope.breakPoints = new Array();
  $scope.showStdIn = true;
  $scope.showArguments = true;
  $scope.currentPrompt = "";
  $scope.stateToPresent = {"STASIS" : "Program paused",
                           "RUNNING" : "Program running",
                           "NO_INFERIOR" : "Program ended"};
  $scope.currentState = "";
  $scope.state = {"runBtn" : false,
                    "stopBtn" : false,
                    "stepOverBtn" : false,
                    "stepIntoBtn" : false,
                    "stepReturnBtn" : false};

  $scope.files = {};

  init();
  displayCodeMirror();
  displayTerminal();
  displayTagit();

  $scope.clickButton = function(destination) {
    if (destination == "run") {
        setCurrentState("RUNNING");
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
    setCurrentState(data.state);
    if (data.line_number) {
      $scope.currentLine = data.line_number;
      $scope.codeMirror.setCursor($scope.currentLine);
      // refactor - plus fix heap!
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

  $scope.$watch('currentLine', function (currentLine, prevLine) {
    console.log(currentLine, prevLine);
    var BACK_CLASS = "CodeMirror-activeline-background";
    if (prevLine >= 0) {
        $scope.codeMirror.removeLineClass(prevLine, "background", BACK_CLASS);
    }
    if (currentLine >= 0) {
        $scope.codeMirror.addLineClass(currentLine, 'background', BACK_CLASS);
    }
  });

  function setCurrentState(state) {
    $scope.currentState = state;
    setButtonState(state);
  }

  function setButtonState(state) {
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
  }

  function init() {
    $http.get('getCurrentState')
      .success(function(data) {
        setCurrentState(data.state);
      })
      .error(function(status) {
        console.log("There is an error getting state.");
    });
  }

  // Invoke a GET method to ask for Java code.
  function displayCodeMirror() {
    $http.get('./main_class.json')
      .success(function(data) {
        if (!data.file_name) {
          alert("Can not load main class please try again");
        }
        if (!data.code) {
          data.code = ["There is an error getting main class code"];
        }
        $scope.currentFileName = data.file_name;
        $scope.files[data.file_name] = {"code" : CodeMirror.Doc(data.code.join(''), 'text/x-java')};
        setUpCodeMirror();
        $scope.codeMirror.swapDoc($scope.files[data.file_name].code);
      })
      .error(function(status) {
        console.log("There is an error main class");
        setUpCodeMirror();
    });
  }

  // Given a file name, this function will get a code from backend and stroe it in files
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
        $scope.files[fileName] = CodeMirror.Doc(data.code.join(''), 'text/x-java');
      })
      .error(function(status) {
        console.log("There is an error getting file " + fileName);
    });
  }

  // Initialze codeMirror and display it
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
      var clas = $scope.currentFileName.substring(0, $scope.currentFileName.indexOf('.'));
      if (info.gutterMarkers) {
        tryToUnsetBreakpoint(cm, clas, line);
      } else {
        tryToSetBreakpoint(cm, clas, line);
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
          if (!$scope.files[clas+".java"].breakpoints) {
            $scope.files[clas+".java"].breakpoints = new Array();
          }
          var breakPoints = $scope.files[clas+".java"].breakpoints;
          breakPoints.push(lineNumber);
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
          $scope.files[clas+".java"].breakpoints = new Array();
          var breakPoints = $scope.files[clas+".java"].breakpoints;
          var index = breakPoints.indexOf(lineNumber);
          if (index > -1) {
            breakpoints.splice(index, 1);
          }
        } else {
          console.log("Could not unset breakpoint.");
        }
      })
      .error(function(data) {
        console.log("Setting breakpoint error.");
    });
  }

  // Invoke a POST method to backend to send a data about a set of breakpoint.
  function setBreakPoints() {
    $http.post('breakPoints', $scope.breakPoints)
      .success(function(data) {
        //console.log(data.status);
      })
      .error(function(data) {
        console.log("There is an error sending break points " + data.status);
    });
  }

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
        // Initial setup for
        greetings: "",
        height: 200,
        width: "100%",
        prompt: INITIAL_PROMPT,
      }
    );
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
    });
  }
});
