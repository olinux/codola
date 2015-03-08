angular.module('codola_overview', [])
    .controller('InstallationController', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
        $http.get('rest/app/installed').success(function (data, status, headers, config) {
            $scope.installation = data;
            if (!$scope.installation.installed) {
                $('#install').modal('show')
            }
        }).error(function (data, status, headers, config) {
            alert('Was not able to get installation state of application');
        });
        $scope.install = function (installationStep) {
            $http.post('rest/app/' + installationStep.postMethod).success(function (data, status, headers, config) {
                $scope.installation = data;
            }).error(function (data, status, headers, config) {
                alert('Was not able to get installation state of application');
            });
        }
    }])
    .controller('MainMenuController', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
        $http.get('rest/documents/').success(function (data, status, headers, config) {
            $scope.documents = data;
        }).error(function (data, status, headers, config) {
            alert('Was not able to fetch already existing documents');
        });
        $scope.loadDefaultDocuments = function() {
            $http.get('rest/documents/default').success(function (data, status, headers, config) {
                $scope.defaultDocuments = data;
            }).error(function (data, status, headers, config) {
                alert('Was not able to fetch already existing default documents');
            });
        };
        $scope.loadDefaultDocuments();
        $scope.markFileForRemoval = function (document) {
            $scope.markedFileForRemoval = document;
        }
        $scope.remove = function () {
            $http.delete('rest/documents/default/' + $scope.markedFileForRemoval.displayName).success(function (data, status, headers, config) {
                $scope.loadDefaultDocuments();
                $('#removeGitDoc').modal('hide')
            }).error(function (data, status, headers, config) {
                alert("Was not able to remove document");
            });
        }


    }]).controller('AddGitDocumentController', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
        $scope.gittype = 'default';
        $scope.maindoc = 'main.tex';
        $http.get('rest/app/publicKey').success(function (data, status, headers, config) {
            $scope.publicKey = data;
        }).error(function (data, status, headers, config) {
            $scope.publicKey = undefined;
        });
        $scope.add = function () {
            if ($scope.gittype == 'default') {
                $http.post('rest/documents/default/' + $scope.docname).success(function (data, status, headers, config) {
                    window.location.href = "editor.html?default/" + $scope.docname;
                }).error(function (data, status, headers, config) {
                    alert("Was not able to add document");
                });

            }
            else {
                alert("Not yet implemented");
            }
        }
    }]);