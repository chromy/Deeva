var deeva = angular.module('deeva', []);

var initial_prompt = '$ '

// Currently is a whole document controller
deeva.controller('SimpleController', function ($scope, $http) {
  $scope.prevLine = 0;
  $scope.currentLine = 1;
  $scope.breakPoints = new Array();
  $scope.showStdIn = false;
  $scope.currentPrompt = "";

  console.log($(".resizable"));
  $(".resizable").resizable();

  // When loaded, invoke a GET method to ask for Java code.
  $http.get('./main_class.json')
    .success(function(data) {
      $scope.file_name = data.file_name;
      $scope.code = data.code;
      $scope.displayCode();
    })
    .error(function(status) {
      $scope.file_name = "Can not load Java code";
      $scope.code = [];
      console.log("There is an error getting json");
      $scope.displayCode();
  });

  // Initialze codeMirror and display it
  $scope.displayCode = function() {
    // Initialize codeMirror
    $scope.codeMirror = CodeMirror(document.getElementById('codeInputPane'), {
      mode: 'text/x-java',
      tabSize: 4,
      lineNumbers: true,
      lineWrapping: true,
      firstLineNumber: 0,
      readOnly: "nocursor",
      gutters: ["CodeMirror-linenumbers", "breakpoints"],
    });
    // Set an event handler when the gutter is clicked
    // which update frontend as well ass invoke a method setBreakPoints
    $scope.codeMirror.on("gutterClick", function(cm, line) {
      var info = cm.lineInfo(line);
      var breakPoint;
      if (info.gutterMarkers) {
        breakPoint = null;
        $scope.breakPoints.splice($scope.breakPoints.indexOf(line), 1);
      } else {
        breakPoint = $scope.makeBreakPoint();
        $scope.breakPoints.push(line);
      }
      cm.setGutterMarker(line, "breakpoints", breakPoint);
      $scope.setBreakPoints();
    });
    // Load an input file to be display in codeMirror
    for (index = 0;index < $scope.code.length;index++) {
      $scope.codeMirror.setLine(index, $scope.code[index]);
    }
    $scope.codeMirror.setCursor($scope.codeMirror.firstLine());
  }

  // Invoke a POST method to backend to send a data about a set of breakpoint.
  $scope.setBreakPoints = function() {
    $http.post('breakPoints', $scope.breakPoints)
      .success(function(data) {
        //console.log(data.status);
      })
      .error(function(data) {
        console.log("There is an error sending break points " + data.status);
    });
  }

  // Return a div that contain a marker for breakpoint.
  $scope.makeBreakPoint = function () {
    var breakPoint = document.createElement("div");
    breakPoint.style.color = "#0000FF";
    breakPoint.innerHTML = "●";
    return breakPoint;
  }

  // High light the current line. The previous line high light is also removed.
  $scope.highLightLine = function() {
    var BACK_CLASS = "CodeMirror-activeline-background";
    $scope.codeMirror.removeLineClass($scope.prevLine, "background", BACK_CLASS);
    $scope.codeMirror.addLineClass($scope.currentLine, 'background', BACK_CLASS);
  }

  // Called by step button which send a POST method to backend infroming step occur
  $scope.step = function() {
    if ($scope.currentLine <= $scope.code.length) {
      $http.post('step')
        .success(function(data) {
          $scope.prevLine = $scope.currentLine;
          $scope.currentLine = data.step_number;
          $scope.highLightLine();
          $scope.codeMirror.setCursor($scope.currentLine);
          $scope.printToTerminal(data.stdout);
          console.log("The current step is " + $scope.currentLine);
        })
        .error(function(status) {
          alert("There is an error on step " + data.step_number);
          console.log("There is an error on step()");
      });
    }
  }
  $scope.start = function() {
    $http.post('start')
      .success(function(data) {
      })
      .error(function(status) {
        alert("There is an error on start.");
        console.log("There is an error on start()");
      });
  };

  $scope.displayTerminal = function() { 
    $scope.terminal = $('#terminal').terminal(function(input, term) {
      $scope.sendInput(input);
      }, {
        greetings: "Welcome to Deeva",
        height: 200,
        width: "100%",
        prompt: initial_prompt,
      }
    );
  }

  $scope.displayTerminal();

  $scope.sendInput = function(input) {
    $http.post('input', input)
      .success(function(data) {
        //console.log(data.status);
      })
      .error(function(data) {
        console.log("There is an error sending input " + data.status);
    });
  }

  $scope.printToTerminal = function(output) { 
    if (output.charAt(output.length - 1).valueOf() == "\n") {
      output = output.substring(0, output.length - 1);
      var remainedToPrint = $scope.currentPrompt.substring(0, $scope.currentPrompt.length);
      $scope.terminal.echo(remainedToPrint + output);
      $scope.terminal.set_prompt(initial_prompt);
    } else {
      $scope.currentPrompt += (output);
      $scope.terminal.set_prompt($scope.currentPrompt + initial_prompt);
    }
  }

});


