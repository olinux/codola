var firebaseUrl = '';

String.prototype.endsWith = function(suffix) {
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

function initCodola(firebase) {
    firebaseUrl = firebase;
}

angular.module('codola', [])
    .controller('MainMenuController', ['$scope', '$rootScope','$http', function ($scope, $rootScope, $http) {
        $http.get('rest/documents/').success(function (data, status, headers, config) {
            $scope.documents = data;
        }).error(function (data, status, headers, config) {
            alert('Was not able to fetch already existing documents');
        });
    }]).controller('AddGitDocumentController', ['$scope', '$rootScope','$http', function ($scope, $rootScope, $http) {
        $scope.gittype='default';
        $scope.maindoc='main.tex';
        $http.get('rest/app/publicKey').success(function (data, status, headers, config) {
            $scope.publicKey = data;
        }).error(function (data, status, headers, config) {
            $scope.publicKey = undefined;
        });
    }])
    .controller('FirepadController', ['$scope', '$rootScope','$http', function ($scope, $rootScope, $http) {
        $scope.filterFiles = true;
        $scope.searchText = '';
        $http.get('rest/documents/' + getIdFromParam() + '/files').success(function (data, status, headers, config) {
            $scope.files = data;
            if($scope.files.length>0){
                var loadFile = $scope.getMainFile();
                if(typeof loadFile=='undefined'){
                    loadFile = $scope.getFirstRootFile();
                }
                $scope.loadFile(loadFile.name);
            }
        }).error(function (data, status, headers, config) {
            alert('Something went wrong');
        });

        $scope.getMainFile=function(){
            for(var i in $scope.files) {
                var file = $scope.files[i];
                if(!file.directory && file.mainFile){
                    return file;
                }
            }
            return undefined;
        }


        $scope.getFirstRootFile=function(){
            for(var i in $scope.files) {
                var file = $scope.files[i];
                if(!file.directory && $scope.showFile(file.name)){
                    return file;
                }
            }
        }

        $scope.fileEndingFilter = [".tex", ".md"];


        $scope.showFile = function(file){
            if(file.indexOf($scope.searchText)<0){
                return false;
            }
            if(!$scope.filterFiles){
                return true;
            }
            for(var ending in $scope.fileEndingFilter){
                if(file.endsWith($scope.fileEndingFilter[ending])){
                    return true;
                }
            }
            return false;
        }

        $scope.hasFile=function(){
            return $scope.currentFile !== '';
        }

        $scope.loadFile = function (file) {
            $('#firepadContainer').empty();
            $scope.currentFile = file;
            if(typeof $scope.firepadRef != 'undefined'){
                $scope.firepadRef.unauth();
            }
            $http.get('rest/documents/' + getIdFromParam() + '/files/' + encodeURIComponent(file)).success(function (data, status, headers, config) {
                $('#firepadContainer').append("<div id=\"firepad\"></div>");
                $scope.firepadRef = new Firebase(firebaseUrl + getIdFromParam()+"/"+$scope.currentFile.replace(/\./g, "_"));
                $scope.editor = ace.edit('firepad');
                $scope.firepad = Firepad.fromACE($scope.firepadRef, $scope.editor, {defaultText: data});
                $scope.editor.getSession().setMode("ace/mode/latex");
                $scope.editor.setTheme("ace/theme/eclipse");
                $rootScope.$emit('documentChanged');
            }).error(function (data, status, headers, config) {
                alert('Can not load file');
            });
        }

        $scope.saveFile = function () {
            $http.put('rest/documents/' + getIdFromParam() + '/files/' + encodeURIComponent($scope.currentFile), $scope.firepad.getText())
                .success(function (data, status, headers, config) {
                    // this callback will be called asynchronously
                    // when the response is available
                    $rootScope.$emit('documentChanged');
                }).
                error(function (data, status, headers, config) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                });
        }
       $(document).keydown(function(event) {
           if (!( String.fromCharCode(event.which).toLowerCase() == 's' && event.ctrlKey) && !(event.which == 19)) return true;
            $scope.saveFile();
            event.preventDefault();
            return false;
        });

    }])
    .controller('PreviewController', ['$scope','$rootScope', function ($scope, $rootScope) {
        $scope.previewData = '';
        $rootScope.$on('documentChanged', function(event){
            document.getElementById('pdfcanvas').contentWindow.location.reload(true);
            $scope.preview();
        });
        $scope.preview = function () {
            $scope.previewData = 'pdf/web/viewer.html?file=../../rest/documents/' + getIdFromParam();
        }
    }]);