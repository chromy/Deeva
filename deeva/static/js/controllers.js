var deeva = angular.module('deeva', []);

var initial_prompt = '';
var speed = "slow";
var enable = 1;
var disable = 0.6;

// Currently is a whole document controller
deeva.controller('SimpleController', function ($scope, $http) {
  $scope.prevLine = 0;
  $scope.currentLine = 1;
  $scope.breakPoints = new Array();
  $scope.showStdIn = true;
  $scope.showArguments = true;
  $scope.currentPrompt = "";
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
    if ($scope.state[destination + "Btn"]) {
      if (destination == "run") {
        setCurrentState("RUNNING");
      }
      if ($scope.currentLine <= $scope.code.length) {
        $http.post(destination)
          .success(function(data) {
            console.log(data);
            updateState(data)
          })
          .error(function(status) {
            console.log("There is an error on " + destination + "()");
          });
      }
    } else {
      console.log(destination + " is disabled");
    }
  }

  function updateState(data) {
    if (!data) {
      return;
    }
    setCurrentState(data.state);
    if (data.line_number) {
      $scope.prevLine = $scope.currentLine;
      $scope.currentLine = data.line_number;
      highLightLine($scope);
      $scope.codeMirror.setCursor($scope.currentLine);
    }
    if (data.stdout) {
      printToTerminal($scope, data.stdout);
    }
  }
  
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
      refreshButtonsWithCurrentState();
      console.log(state)
    }
  }
  
  function refreshButtonsWithCurrentState() {
    for (button in $scope.state) {
      buttonState = $scope.state[button]? enable : disable;
      $("#" + button).fadeTo(speed, buttonState);
    }
   }

  function init($scope, $http) {
    $http.get('getCurrentState')
      .success(function(data) {
        setCurrentState(data.state);  
      })
      .error(function(status) {
        console.log("There is an error getting ")
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
        console.log("There is an error getting json");
        setUpCodeMirror($scope);
    });
  };

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
      var breakPoint;
      if (info.gutterMarkers) {
        breakPoint = null;
        $scope.breakPoints.splice($scope.breakPoints.indexOf(line), 1);
      } else {
        breakPoint = makeBreakPoint();
        $scope.breakPoints.push(line);
      }
      cm.setGutterMarker(line, "breakpoints", breakPoint);
      setBreakPoints($scope, $http);
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
  function setBreakPoints($scope, $http) {
    $http.post('breakPoints', $scope.breakPoints)
      .success(function(data) {
        //console.log(data.status);
      })
      .error(function(data) {
        console.log("There is an error sending break points " + data.status);
    });
  }

  // High light the current line. The previous line high light is also removed.
  function highLightLine($scope) {
    var BACK_CLASS = "CodeMirror-activeline-background";
    $scope.codeMirror.removeLineClass($scope.prevLine, "background", BACK_CLASS);
    $scope.codeMirror.addLineClass($scope.currentLine, 'background', BACK_CLASS);
  };

  function printToTerminal($scope, output) {
    if (!output) {
      return;
    }
    if (output.slice(-1) == "\n") {
      output = output.substring(0, output.length - 1);
      var remainedToPrint = $scope.currentPrompt.substring(0, $scope.currentPrompt.length);
      $scope.terminal.echo(remainedToPrint + output);
      $scope.terminal.set_prompt(initial_prompt);
    } else {
      $scope.currentPrompt += (output);
      $scope.terminal.set_prompt($scope.currentPrompt + initial_prompt);
    }
  }

  function displayTerminal($scope) {
    $scope.terminal = $('#terminal').terminal(function(input, term) {
      // This function is called whenever the enter is hit.
      sendInput($scope, input);
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

  var tabs = $('.tabs > li');

  tabs.on("click", function(){
    tabs.removeClass('active');
    $(this).addClass('active');
  });

});
