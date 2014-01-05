'use strict';

var directives = angular.module("deeva.directives", []);

directives.directive('deevaPackage', ['PackageService', function(PackageService) {
    /* String that gets displayed when we select a new package */
    var new_string = "Select package or source file";

    return {
        restrict: 'E',
        templateUrl: 'static/templates/deevaPackage.html',
        scope: {
            packageDir: '=',
            breadcrumb: '=',
            sourceClick: '=', /* Callback for when source is clicked */
            currentClass: '='
        },

        link: function(scope, element, attrs) {
            /* Given a package directory, and an index into the
             * breadcrumb, get the packages situated at the last but one
             * package in the breadcrumb */
            scope.getPackages = function(dir, index) {
                var current_pkg_path = scope.breadcrumb.slice(0, index);
                var current_pkg = PackageService.navigate(scope.packageDir, current_pkg_path);
                return PackageService.show_packages(current_pkg);
            },

            /* Given a package directory, and an index into the
             * breadcrumb, get the sources situated at the last but one
             * package in the breadcrumb */
            scope.getSources = function(dir, index) {
                var current_pkg_path = scope.breadcrumb.slice(0, index);
                var current_pkg = PackageService.navigate(scope.packageDir, current_pkg_path);
                return PackageService.show_sources(current_pkg);
            },

            /* Auxillary function to tell whether an object is empty */
            scope.isEmpty = function(object) {
                return Object.keys(object).length == 0;
            },

            /* Auxillary function to tell whether we just selected a package */
            scope.pkg_was_selected = function(crumb_string) {
                return new_string == crumb_string;
            },

            /* Event handler for when a package in the dropdown is clicked */
            scope.selectPackage = function(index, package_name) {
                /* If we've just selected a package, it will display the
                 * `new_string', if so, then we do nothing. */

                if (package_name == scope.breadcrumb[index]) {
                    return;
                }

                if (scope.breadcrumb[length - 1] == new_string) {
                    index--;
                }

                var new_breadcrumb = scope.breadcrumb.slice(0, index);

                new_breadcrumb.push(package_name);
                new_breadcrumb.push(new_string);

                scope.breadcrumb = new_breadcrumb;
            },

            /* Event handler for when a source file in the dropdown is clicked */
            scope.selectSource = function(index, short_classname, full_classname) {
                var new_breadcrumb = scope.breadcrumb.slice(0, index);

		/* If there are no packages, we select the default package */
		if (new_breadcrumb == []) {
		    new_breadcrumb.push('(default)');
		}
                new_breadcrumb.push(short_classname);

                /* Delegate method to caller */
                scope.sourceClick(full_classname, function() {
                    scope.breadcrumb = new_breadcrumb;
                });
            }

            scope.isCurrentClass = function() {
                if (scope.currentClass.indexOf(".") < 0) {
                    return scope.breadcrumb[scope.breadcrumb.length - 1] == scope.currentClass;
                }

                return scope.breadcrumb.join(".") == scope.currentClass;
            };

            scope.selectCurrentClass = function() {

                var new_breadcrumb = scope.currentClass.split(".");
                var last_index = new_breadcrumb.length - 1;
                var short_classname = new_breadcrumb[last_index];

                if (new_breadcrumb.slice(0, last_index).length == 0) {
                    new_breadcrumb = ['(default)'].concat([new_breadcrumb[last_index]]);
                    last_index++;
                }

                scope.breadcrumb = new_breadcrumb;
                scope.selectSource(last_index, short_classname, scope.currentClass);
            }
        },
    }
}]);
