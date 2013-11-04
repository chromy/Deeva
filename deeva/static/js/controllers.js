var deeva = angular.module('deeva', []);

var initial_prompt = '$ '

// Currently is a whole document controller
deeva.controller('SimpleController', function ($scope, $http) {
  $scope.prevLine = 0;
  $scope.currentLine = 1;
  $scope.breakPoints = new Array();
  $scope.showStdIn = true;
  $scope.currentPrompt = "";
  $scope.canStep = false;
  $scope.canRun = true;
  $scope.canStop = false;
  $scope.canStepInto = false;
  $scope.canStepReturn = false;

  $(".resizable").resizable();
  displayCodeMirror($scope, $http);
  displayTerminal($scope);

  // Called by step button which send a POST method to backend infroming step occur
  $scope.step = function() {
    if ($scope.currentLine <= $scope.code.length) {
      $http.post('step')
        .success(function(data) {
          $scope.prevLine = $scope.currentLine;
          $scope.currentLine = data.step_number;
          highLightLine($scope);
          $scope.codeMirror.setCursor($scope.currentLine);
          printToTerminal($scope, data.stdout);
          console.log("The current step is " + $scope.currentLine);
        })
        .error(function(status) {
          //alert("There is an error on step " + data.step_number);
          console.log("There is an error on step()");
      });
    }
  }

  // Called by a run button which send a POST method to backend to invoke run

  $scope.run = function() {
    $scope.canRun = false;
    $scope.canStep = true;
    $scope.canStop = true;
    $scope.canStepInto = true;
    $scope.canStepReturn = true;
    $http.post('run')
      .success(function(data) {
      })
      .error(function(status) {
        alert("There is an error on run.");
        console.log("There is an error on run()");
      });
  };

  // Called by a stop button which send a POST method to backend to invoke stop
  $scope.stop = function() {
    $scope.canRun = true;
    $scope.canStep = false;
    $scope.canStop = false;
    $scope.canStepInto = false;
    $scope.canStepReturn = false;
  };

  // Called by a step-into button which send a POST method to backend to invoke step-into
  $scope.step_into = function() {
    $scope.canStep = true;
    $scope.canStop = true;
    $scope.canStepInto = true;
    $scope.canStepReturn = true;
  };

 // Called by a step-return button which send a POST method to backend to invoke step-return
  $scope.step_return = function() {
    $scope.canStep = true;
    $scope.canStop = true;
    $scope.canStepInto = true;
    $scope.canStepReturn = true;
  };

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
    console.log(output + "is output end with newline?");
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
        greetings: "Welcome to Deeva",
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
  
  function displayTag-it() {
    $("#arguments").tagit({
    fieldName: "skills"
});
  }

});


