/*
 * Copyright © 2015 The CoDoLa developer team
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

var module = angular.module('codola_overview', []);

module.controller('InstallationController', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
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
}]);

module.controller('MainMenuController', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
    $scope.loadUploadedDocuments = function () {
        $http.get('rest/documents/uploads').success(function (data, status, headers, config) {
            $scope.uploadedDocuments = data;
        }).error(function (data, status, headers, config) {
            alert('Was not able to fetch uploaded documents');
        });
    };
    $scope.loadDefaultDocuments = function () {
        $http.get('rest/documents/default').success(function (data, status, headers, config) {
            $scope.defaultDocuments = data;
        }).error(function (data, status, headers, config) {
            alert('Was not able to fetch already existing default documents');
        });
    };
    $scope.loadDedicatedDocuments = function () {
        $http.get('rest/documents/dedicated').success(function (data, status, headers, config) {
            $scope.dedicatedDocuments = data;
        }).error(function (data, status, headers, config) {
            alert('Was not able to fetch dedicated documents');
        });
    };
    $scope.loadUploadedDocuments();
    $scope.loadDedicatedDocuments();
    $scope.loadDefaultDocuments();

    $scope.doRemove = function () {
        $http.delete('rest/documents/default/' + $scope.markedFileForRemoval.displayName).success(function (data, status, headers, config) {
            $scope.loadDefaultDocuments();
            $('#removeGitDoc').modal('hide')
        }).error(function (data, status, headers, config) {
            alert("Was not able to remove document");
        });
    }
    $scope.doDetach = function () {
        $http.delete('rest/documents/' + $scope.markedFileForRemoval.gitRepository + '/' + $scope.markedFileForRemoval.displayName).success(function (data, status, headers, config) {
            $scope.loadDedicatedDocuments();
            $('#detachGitDoc').modal('hide')
        }).error(function (data, status, headers, config) {
            alert("Was not able to detach document");
        });
    }
    $scope.doRemoveUploaded = function () {
        $http.delete('rest/documents/uploads/' + $scope.markedFileForRemoval.displayName).success(function (data, status, headers, config) {
            $scope.loadUploadedDocuments();
            $('#removeUploadedDoc').modal('hide')
        }).error(function (data, status, headers, config) {
            alert("Was not able to remove uploaded document");
        });
    }

    $scope.remove = function (document) {
        $scope.markedFileForRemoval = document;
        $("#removeGitDoc").modal("show");
    }
    $scope.detach = function (document) {
        $scope.markedFileForRemoval = document;
        $("#detachGitDoc").modal("show");
    }

    $scope.removeUploaded = function (document) {
        $scope.markedFileForRemoval = document;
        $("#removeUploadedDoc").modal("show");
    }


}]);


module.controller('AddGitDocumentController', ['$scope', '$rootScope', '$http', function ($scope, $rootScope, $http) {
    $scope.gittype = 'default';
    $scope.maindoc = 'main.tex';
    $http.get('rest/app/publicKey').success(function (data, status, headers, config) {
        $scope.publicKey = data;
    }).error(function (data, status, headers, config) {
        $scope.publicKey = undefined;
    });
    $scope.add = function () {
        if ($scope.gittype == 'default') {
            $http.post('rest/documents/default/' + $scope.docname, $scope.maindoc).success(function (data, status, headers, config) {
                window.location.href = "editor.html?default/" + $scope.docname + $scope.docname;
            }).error(function (data, status, headers, config) {
                alert("Was not able to add document");
            });

        }
        else {
            $http.post('rest/documents/' + $scope.docname, $scope.giturl).success(function (data, status, headers, config) {
                window.location.href = "editor.html?" + $scope.docname + "/" + $scope.docname;
            }).error(function (data, status, headers, config) {
                alert("Was not able to add document");
            });
        }
    }
}]);