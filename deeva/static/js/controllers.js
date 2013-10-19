var deeva = angular.module('deeva', []);

deeva.controller('SimpleController', function ($scope, $http) {
  $scope.prevLine = 0;
  $scope.currentLine = 1;
  $scope.breakPoints = new Array();

  $http.get('./javacode.json')
    .success(function(data) {
      $scope.codeName = data.codeName;
      $scope.code = data.code;
      $scope.displayCode();
    })
    .error(function(status) {
      $scope.codeName = "Can not load Java code";
      $scope.code = [];
      console.log("There is an error getting json");
      $scope.displayCode();
  });

  $scope.displayCode = function() {
    $scope.codeMirror = CodeMirror(document.getElementById('codeInputPane'), {
      mode: 'text/x-java',
      tabSize: 4,
      lineNumbers: true,
      lineWrapping: true,
      firstLineNumber: 0,
      readOnly: "nocursor",
      gutters: ["CodeMirror-linenumbers", "breakpoints"],
    });
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
      console.log($scope.breakPoints);
    });
    for (index = 0;index < $scope.code.length;index++) {
      $scope.codeMirror.setLine(index, $scope.code[index]);
    }
  }

  $scope.setBreakPoints = function() {
    $http.post('breakPoints', $scope.breakPoints)
      .success(function(data) {
        console.log(data.status);
      })
      .error(function(data) {
        console.log("There is an error sending break points " + data.status);
    });
  }

  $scope.makeBreakPoint = function () {
    var breakPoint = document.createElement("div");
    breakPoint.style.color = "#0000FF";
    breakPoint.innerHTML = "●";
    return breakPoint;
  }

  $scope.step = function() {
    if ($scope.currentLine <= $scope.code.length) {
      $http.post('step')
        .success(function(data) {
          $scope.prevLine = $scope.currentLine;
          $scope.currentLine = data.step_number;
          $scope.highLightLine();
          console.log("The current step is " + $scope.currentLine);
        })
        .error(function(status) {
          alert("There is an error on step " + data.step_number);
          console.log("There is an error on step()");
      });
    }
  }

  $scope.highLightLine = function() {
    var BACK_CLASS = "CodeMirror-activeline-background";
    $scope.codeMirror.removeLineClass($scope.prevLine, "background", BACK_CLASS);
    $scope.codeMirror.addLineClass($scope.currentLine, 'background', BACK_CLASS);
  }

});


