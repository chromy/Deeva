var deeva = angular.module('deeva', []);

var initial_prompt = '';

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

  init($scope, $http);
  displayCodeMirror($scope, $http);
  displayTerminal($scope);
  displayTagit($scope);

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
    }
    if (data.stdout) {
      printToTerminal($scope, data.stdout, false);
    }
    if (data.stderr) {
      printToTerminal($scope, data.stderr, true);
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

  function init($scope, $http) {
    $http.get('getCurrentState')
      .success(function(data) {
        setCurrentState(data.state);
      })
      .error(function(status) {
        console.log("There is an error getting state.");
    });
  }

  // Invoke a GET method to ask for Java code.
  function displayCodeMirror($scope, $http) {
    $http.get('./main_class.json')
      .success(function(data) {
        $scope.file_name = data.file_name;
        $scope.code = data.code;
        setUpCodeMirror($scope);
      })
      .error(function(status) {
        $scope.file_name = "Can not load Java code";
        $scope.code = [];
        console.log("There is an error main class");
        setUpCodeMirror($scope);
    });
  }

  // Initialze codeMirror and display it
  function setUpCodeMirror($scope) {
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
    // Load an input file to be display in codeMirror
    for (index = 0;index < $scope.code.length;index++) {
      $scope.codeMirror.setLine(index, $scope.code[index]);
    }
    $scope.codeMirror.setCursor($scope.codeMirror.firstLine());
    setGutterHandler($scope);
  }

  // Set an event handler when the gutter is clicked
  // which update frontend as well ass invoke a method setBreakPoints
  function setGutterHandler($scope) {
      $scope.codeMirror.on("gutterClick", function(cm, line) {
      var info = cm.lineInfo(line);
      // XXX: Horrible hack
      var clas = $scope.file_name.substring(0, $scope.file_name.indexOf('.'));
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
        } else {
          console.log("Could not unset breakpoint.");
        }
      })
      .error(function(data) {
        console.log("Setting breakpoint error.");
    });
  }

  // Invoke a POST method to backend to send a data about a set of breakpoint.
  function setBreakPoints($scope, $http) {
    $http.post('breakPoints', $scope.breakPoints)
      .success(function(data) {
        //console.log(data.status);
      })
      .error(function(data) {
        console.log("There is an error sending break points " + data.status);
    });
  }

  function printToTerminal($scope, output, isErr) {
    if (!output) {
      return;
    }
    var lines = output.split("\n");
    for (index = 0;index<lines.length;index++) {
      line = lines[index];
      if (index == lines.length - 1) {
        $scope.currentPrompt += (line);
        $scope.terminal.set_prompt($scope.currentPrompt + initial_prompt);
      } else {
        var toPrint = $scope.currentPrompt + line;
        toPrint = isErr ? ("[[;#FF0000;#fff]" + toPrint + "]") : toPrint;
        $scope.terminal.echo(toPrint);
        $scope.currentPrompt = initial_prompt;
        $scope.terminal.set_prompt(initial_prompt + initial_prompt);
      }
    }
    /*if (output.slice(-1) == "\n") {
      output = output.substring(0, output.length - 1);
      var remainedToPrint = $scope.currentPrompt.substring(0, $scope.currentPrompt.length);
      $scope.terminal.echo(remainedToPrint + output);      
      $scope.terminal.set_prompt(initial_prompt);
    } else {
      $scope.currentPrompt += (output);
      $scope.terminal.set_prompt($scope.currentPrompt + initial_prompt);
    }*/
  }

  function displayTerminal($scope) {
    $scope.terminal = $('#terminal').terminal(function(input, term) {
      // This function is called whenever the enter is hit.
      if (input == "") {
        printToTerminal($scope, "\n", false);
      } else {
        sendInput($scope, input);
      }
      }, {
        // Initial setup for
        greetings: "",
        height: 200,
        width: "100%",
        prompt: initial_prompt,
      }
    );
  }

  function sendInput($scope, input) {
    $scope.terminal.set_prompt(initial_prompt);
    $http.post('input', input)
      .success(function(data) {
      })
      .error(function(data) {
        console.log("There is an error sending input " + data.status);
    });
  }

  function displayTagit($scope) {
    //Use the function below to get all arguments
    //console.log($scope.arguments.tagit("assignedTags"));
    $scope.arguments = $("#arguments").tagit({
      allowDuplicates: true,
    });
  }
   

});
