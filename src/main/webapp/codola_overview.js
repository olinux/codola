angular.module('codola_overview', [])
    .controller('InstallationController', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
        $http.get('rest/app/installed').success(function (data, status, headers, config) {
            $scope.installation = data;
            if (!$scope.installation.installationStarted) {
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
    }]).controller('AddGitDocumentController', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
        $scope.gittype = 'default';
        $scope.maindoc = 'main.tex';
        $http.get('rest/app/publicKey').success(function (data, status, headers, config) {
            $scope.publicKey = data;
        }).error(function (data, status, headers, config) {
            $scope.publicKey = undefined;
        });
    }]);