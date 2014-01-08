'use strict';

var services = angular.module("deeva.services", []);

/*
 *  Service to handle package traversal (auxillary functions)
 */
services.service('PackageService', [function() {
    /* Returns the packages from a package directory */
    function packages(pkg_dir) {
        var result = {};

        angular.forEach(pkg_dir, function(value, key) {
            if (key != '&sources' && key.indexOf('$$') == -1) {
                result[key] = value;
            }
        });

        return result;
    }

    /* Returns the sources from a package directory */
    function sources(pkg_dir) {
        return pkg_dir && pkg_dir['&sources'] ? pkg_dir['&sources'] : {};
    }

    /* Navigates within a package directory to a certain package */
    function navigate(package_dir, package_location) {
        if (package_dir == null || package_location == null) {
            return null;
        }

        /* Navigate to given package location */
        var current_location = package_dir;
        for (var i = 0; i < package_location.length; i++) {
            var pkg_name = package_location[i];
            if (pkg_name.indexOf("&") != -1) {
                return null;
            }

            var pkg = current_location[pkg_name];
            if (pkg == undefined) {
                return null;
            }

            current_location = pkg;
        }
        return current_location;
    }

    return {
        'navigate' : navigate,
        'show_packages' : packages,
        'show_sources' : sources
    };
}]);

/*
 *  Misc Service with auxillary functions
 */
services.service('MiscService', [function() {
    return {
        'nullFunction' : function() {}
    };
}]);

/*
 *  Service to handle the retrieval of files.
 */
services.service('FileService', ['$http', 'PackageService', function($http, PackageService) {
    var files = {};
    var package_dir = null;

    function getPackages(callback) {
        callback = callback || MiscService.nullFunction;
        /* Get cached result */
        if (package_dir != null) {
            callback(package_dir);
            return;
        }

        /* Send request to get the package directory structure */
        $http.get('/files')
            .success(function(data) {
                if (!data.package_dir) {
                    console.error("Server did not return the package directory.");
                    callback(null)
                }

                package_dir = data.package_dir;

                /* Call the callback when we're done retrieving the package directory */
                callback(package_dir);
            })
            .error(function(status) {
                console.error("Error getting package directory");
            });
    }

    function getFile(classname, callback, forced) {
        /* Default value for forced is false */
        forced = typeof forced !== 'undefined' ? forced : false;
        callback = callback || MiscService.nullFunction;

        /* Get cached result */
        if (files[classname] != undefined && !forced) {
            /* Call the callback when we're done */
            callback(files[classname]);
            return;
        }

        /* Retrieve file either for the first time or if forced */
        var url_name = '/file/' + classname;
        $http.get(url_name)
            .success(function(data) {
                var message; // stupid Javascript scoping
                var mime_type = 'text/x-java';
                var errorneous = false;

                if (!data.classname || data.classname != classname) {
                    message = "Server did not return any files.";
                    console.error(message);
                    data.code = [message];
                    mime_type = 'text/x-markdown';
                    errorneous = true;
                }

                if (!data.code) {
                    message = "There was an error getting the code";
                    console.error(message);
                    data.code = [message];
                    mime_type = 'text/x-markdown';
                    errorneous = true;
                }

                if (!data.breakpoints) {
                    message = "There was an error getting the breakpoints";
                    console.error(message);
                    data.breakpoints = [];
                    errorneous = true;
                }

                /* Create new CodeMirror document (maybe refactor this out) */
                files[classname] = {'code': CodeMirror.Doc(data.code.join(''), mime_type),
                                    'breakpoints': data.breakpoints};

                if (!errorneous) {
                    console.log("File retrieved: " + classname);
                }

                /* Call the callback when we're done getting the file */
                callback(files[classname]);
            })
            .error(function(status) {
                var message = "Error getting: " + classname;
                console.error(message);
            });
    }

    function breakpoints (classname, callback) {
        callback = callback || MiscService.nullFunction;
        getFile(classname, function(classdata) {
            var breakpoints = classdata.breakpoints;
            callback(breakpoints);
        });
    }

    function addBreakpoint (classname, line) {
        getFile(classname, function(classdata) {
            var breakpoints = classdata.breakpoints;
            if (breakpoints.indexOf(line) != -1) {
                return;
            }
            breakpoints.push(line);
        });
    }

    function removeBreakpoint (classname, line) {
        getFile(classname, function(classdata) {
            var breakpoints = classdata.breakpoints;
            /* Find and remove a previously set breakpoint */
            var index = breakpoints.indexOf(line);
            if (index > -1) {
                breakpoints.splice(index, 1);
            }
        });
    }

    return {
        'getPackages' : getPackages,
        'getFile' : getFile,
        'breakpoints' : breakpoints,
        'addBreakpoint' : addBreakpoint,
        'removeBreakpoint' : removeBreakpoint,
    };
}]);

