String.prototype.endsWith = function (suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

function getIdFromParam() {
    var id = window.location.search;
    if (id.length > 0) {
        return id.substr(1);
    }
    else {
        alert('Please open this page with an identifier in the hash value!');
    }
}

angular.module('codola_editor', ['blueimp.fileupload', 'ngSanitize'])
    .controller('FirepadController', ['$scope', '$rootScope', '$http', '$sce', function ($scope, $rootScope, $http, $sce) {
        $rootScope.$on('filesChanged', function (event) {
            $scope.loadFileList();
        });

        $http.get('rest/app/firebaseUrl').success(function (data, status, headers, config) {
            $scope.firebaseURL = data;
            $scope.filterFiles = true;
            $scope.searchText = '';
            $scope.loadFileList();
        }).error(function (data, status, headers, config) {
            alert('Was not able to get firebase url');
        });

        $scope.loadFileList = function(){
            $http.get('rest/documents/' + getIdFromParam() + '/files').success(function (data, status, headers, config) {
                $scope.files = data;
                if ($scope.files.length > 0) {
                    var loadFile = $scope.getMainFile();
                    if (typeof loadFile == 'undefined') {
                        loadFile = $scope.getFirstRootFile();
                    }
                    $scope.loadFile(loadFile.name);
                }
            }).error(function (data, status, headers, config) {
                alert('Something went wrong');
            });
        };

        $scope.getMainFile = function () {
            for (var i in $scope.files) {
                var file = $scope.files[i];
                if (!file.directory && file.mainFile) {
                    return file;
                }
            }
            return undefined;
        };


        $scope.getFirstRootFile = function () {
            for (var i in $scope.files) {
                var file = $scope.files[i];
                if (!file.directory && $scope.showFile(file.name)) {
                    return file;
                }
            }
        };

        $scope.fileEndingFilter = [".tex", ".md", ".jpg", ".png", ".gif"];


        $scope.showFile = function (file) {
            if (file.indexOf($scope.searchText) < 0) {
                return false;
            }
            if (!$scope.filterFiles) {
                return true;
            }
            for (var ending in $scope.fileEndingFilter) {
                if (file.endsWith($scope.fileEndingFilter[ending])) {
                    return true;
                }
            }
            return false;
        };

        $scope.hasFile = function () {
            return $scope.currentFile !== '';
        };

        $scope.loadFile = function (file) {
            $('#editor').empty();
            $scope.currentFile = file;
            if (typeof $scope.firepadRef != 'undefined') {
                $scope.firepadRef.unauth();
            }
            $scope.firepadRef = undefined;
            if(file.endsWith(".jpg") || file.endsWith(".png") || file.endsWith(".gif")){
                $('#editor').append("<img src=\"rest/documents/" + getIdFromParam() + "/files/" + encodeURIComponent(file) + "\" class=\"imagePreview\"></img>");
            }
            else {
                $http.get('rest/documents/' + getIdFromParam() + '/files/' + encodeURIComponent(file)).success(function (data, status, headers, config) {
                    $('#editor').append("<div id=\"firepad\"></div>");
                    $scope.firepadRef = new Firebase($scope.firebaseURL + getIdFromParam() + "/" + $scope.currentFile.replace(/\./g, "_"));
                    $scope.editor = ace.edit('firepad');
                    $scope.firepad = Firepad.fromACE($scope.firepadRef, $scope.editor, {defaultText: data});
                    $scope.editor.getSession().setMode("ace/mode/latex");
                    $scope.editor.setTheme("ace/theme/eclipse");
                    $rootScope.$emit('documentChanged');
                }).error(function (data, status, headers, config) {
                    alert('Can not load file');
                });
            }
        };

        $scope.saveFile = function () {

            $http.put('rest/documents/' + getIdFromParam() + '/files/' + encodeURIComponent($scope.currentFile), $scope.firepad.getText())
                .success(function (data, status, headers, config) {
                    if(data!==''){
                        $scope.log =  data.replace(/\n/g, "<br/>");
                        $rootScope.$emit('logAvailable');
                    }
                    // this callback will be called asynchronously
                    // when the response is available
                    $rootScope.$emit('documentChanged');
                }).
                error(function (data, status, headers, config) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    alert("Was not able to save the file");
                });
        };

        $scope.removeFile = function(){
            $http.delete('rest/documents/' + getIdFromParam() + '/files/' + encodeURIComponent($scope.currentFile))
                .success(function (data, status, headers, config) {
                    // this callback will be called asynchronously
                    // when the response is available
                    $rootScope.$emit('filesChanged');
                }).
                error(function (data, status, headers, config) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                });
        }

        $(document).keydown(function (event) {
            if (!( String.fromCharCode(event.which).toLowerCase() == 's' && event.ctrlKey) && !(event.which == 19)) return true;
            $scope.saveFile();
            event.preventDefault();
            return false;
        });

    }])
    .controller('PreviewController', ['$scope', '$rootScope', function ($scope, $rootScope) {
        $scope.previewData = '';
        $scope.scroll=0;
        $rootScope.$on('documentChanged', function (event) {
            document.getElementById('pdfcanvas').contentWindow.location.reload(true);
            $scope.preview();
        });
        $scope.preview = function () {
            $scope.previewData = 'pdf/web/viewer.html?file=../../rest/documents/' + getIdFromParam();
        }
    }]).controller('UploadController', [
        '$scope', '$rootScope', '$http',
        function ($scope, $rootScope, $http) {
            $scope.options = {
                url: 'rest/documents/'+getIdFromParam()+'/files'
            };
            $scope.loadingFiles = false;
            $scope.queue = [];
            $scope.$on('fileuploadstop', function(event, files){
                $rootScope.$emit('filesChanged');
            });
        }
    ]).controller('CreateController', [
        '$scope', '$rootScope', '$http',
         function ($scope, $rootScope, $http) {
            $scope.createFile = function() {
                var fileName;
                if(typeof($scope.path)!='undefined'){
                    fileName = $scope.path+'/'+$scope.filename;
                }
                else{
                    fileName = $scope.filename;
                }
                $http.post('rest/documents/' + getIdFromParam() + '/files/' + encodeURIComponent(fileName))
                    .success(function (data, status, headers, config) {
                        // this callback will be called asynchronously
                        // when the response is available
                        $rootScope.$emit('filesChanged');
                    }).
                    error(function (data, status, headers, config) {
                        // called asynchronously if an error occurs
                        // or server returns response with an error status.
                    });
            };
        }
    ]).controller('PushController', [
        '$scope', '$rootScope', '$http',
        function ($scope, $rootScope, $http) {
            $scope.push = function(){
                $http.put('rest/documents/' + getIdFromParam(), $scope.message)
                    .success(function (data, status, headers, config) {

                    }).
                    error(function (data, status, headers, config) {
                        alert("Push failed: "+status);
                    });
            }
            $scope.options = {
                url: 'rest/documents/'+getIdFromParam()+'/files'
            };
            $scope.loadingFiles = false;
            $scope.queue = [];
            $scope.$on('fileuploadstop', function(event, files){
                $rootScope.$emit('filesChanged');
            });
        }
    ]).controller('NavigationController', [
        '$scope', '$rootScope', '$http',
        function ($scope, $rootScope, $http) {
            $scope.logAvailable=false;
            $scope.refresh = function(){
                $rootScope.$emit('documentChanged');
            }
            $rootScope.$on('logAvailable', function (event) {
                $scope.logAvailable=true;
            });
        }

    ])
;