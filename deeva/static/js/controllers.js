var deeva = angular.module('deeva', []);

deeva.controller('SimpleController', function ($scope, $http) {
  $http.get('./javacode.json')
    .success(function(data) {
      $scope.codeName = data.codeName;
      $scope.code = data.code;
      displayCode($scope);
    })
    .error(function(status) {
      $scope.codeName = "Can not load Java code";
      $scope.code = [];
      console.log("There is an error getting json");
      displayCode($scope);
  });

});

function displayCode($scope) {
  codeMirror = CodeMirror(document.getElementById('codeInputPane'), {
  mode: 'text/x-java',
  lineNumbers: true,
  tabSize: 4,
  indentUnit: 4,
  iewportMargin: 0,
  readOnly: "nocursor",
  extraKeys: {
    Tab: function(cm) {
      var lo = cm.getCursor("start").line;
      var hi = cm.getCursor("end").line;
      for (var i = lo; i <= hi; i++) {
        cm.indentLine(i, "smart");
      }
      cm.setCursor(cm.getCursor("end"));
      return true;
    }
  }
  });
  for (index = 0;index < $scope.code.length;index++) {
    codeMirror.setLine(index, $scope.code[index]);
  }
}
