var deeva = angular.module('deeva', []);

deeva.controller('SimpleController', function ($scope, $http) {
  $http.get('./javacode.json')
    .success(function(data) {
      $scope.codeName = data.codeName;
      $scope.code = data.code;
      codeMirror = CodeMirror(document.getElementById('codeInputPane'), {
        value: $scope.code,
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
            for (var i = lo; i <= hi; i++)
              cm.indentLine(i, "smart");
            cm.setCursor(cm.getCursor("end"));
            return true;
          }
        }
      });
    })
    .error(function(data, status) {
      console.log("There is an error getting json");
  });  
});
