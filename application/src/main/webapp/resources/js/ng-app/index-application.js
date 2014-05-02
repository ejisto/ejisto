/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
 *
 * Ejisto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ejisto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function () {
    "use strict";

    var index = angular.module('indexApplication', ['ui.bootstrap',
        'pascalprecht.translate', 'FieldEditor',
        'ContainerManager', 'BaseServices',
        'WebApplicationManager', 'knalli.angular-vertxbus',
        'Utilities', 'angularFileUpload', 'xeditable'
    ]);

    index.run(function(editableOptions, editableThemes) {
        editableThemes.bs3.submitTpl = '<button type="submit" class="btn btn-primary"><span class="fa fa-check"></span></button>';
        editableThemes.bs3.cancelTpl = '<button type="button" class="btn btn-default" ng-click="$form.$cancel()">'+
                '<span class="fa fa-times"></span>'+
                '</button>';
        editableOptions.theme = 'bs3';
    });

    index.config(function ($translateProvider) {
        $translateProvider.useUrlLoader("/translations");
        $translateProvider.preferredLanguage('en');
    });

    var WizardController = function($scope, FieldService, $upload, vertxEventBusService) {
        $scope.descriptor = {};
        $scope.cancel = function() {
            if(confirm("cancel?")) {
                $scope.$dismiss('canceled');
            }
        };
        $scope.finished = function() {
            $scope.$close(true);
        };
        $scope.fileSelected = function(element) {
            $scope.$apply(function(scope) {
                var reader = new FileReader();
                var url = reader.readAsDataURL(element.files[0]);
            });
        };
        $scope.commitSelectFile = function(step) {
            var descriptor = $scope.descriptor;
            return FieldService.getFieldsGrouped();
        };

        $scope.onFileSelect = function($files) {
            $scope.loading=true;
            _.forEach($files, function(file) {
                $scope.upload = $upload.upload({
                   url: '/application/new/upload',
                   method: 'PUT',
                   file: file
                }).success(function (data, status, headers, config) {
                    $scope.sessionID=data.sessionID;
                    $scope.includedJars=data.resources;
                    $scope.fileName=file.name;
                    $scope.loading=false;
                    $scope.newUploadRequested = false;
                }).error(function() {
                    $scope.loading=false;
                });
            });
        };
        $scope.requestNewUpload = function() {
            $scope.newUploadRequested = true;
            $scope.extractionCompleted = false;
        };
        vertxEventBusService.on('StartFileExtraction', function() {
            $scope.fileExtractionInProgress=true;
        });
        vertxEventBusService.on('FileExtractionProgress', function(progress) {
            var progressStatus = JSON.parse(progress);
            if(progressStatus.taskCompleted) {
                $scope.fileExtractionInProgress=false;
            } else {
                $scope.progressStatus = progressStatus.message;
                $scope.extractionCompleted = true;
            }
        });

    };

    index.controller('MenuController', function ($scope, $modal, $log) {
        $scope.installNewApplication = function() {
            var wizard = $modal.open({
                templateUrl:'/resources/templates/wizard/application-installer-index.html',
                backdrop: 'static',
                keyboard: false,
                controller: WizardController
            });
            wizard.result.then(function() {
                $log.debug("closed");
            });
        };
    });

    index.controller('PropertiesEditorController', function ($scope, FieldService, $log) {
        $scope.selectedEditor = 'HIERARCHICAL';
        FieldService.getFieldsGrouped().then(function(result) {
            $scope.fields = result.data;
        });
        $scope.updateField = function(element, data) {
            return FieldService.updateField(element, data);
        };
    });

    index.controller('ErrorController', function($scope, $rootScope) {
        $rootScope.$on('applicationError', function(message) {
            $scope.errorMessage = message;
        });
    });

    index.controller('ContainersController', function($scope, ContainerService, vertxEventBusService) {
        var loadContainers = function() {
            ContainerService.getRegisteredContainers().success(function(data) {
                $scope.containers = data;
                $scope.loading = false;
            });
        };
        loadContainers();
        vertxEventBusService.on('ContainerStatusChanged', loadContainers);
        $scope.startContainer = function(container) {
            ContainerService.startContainer(container).then(function() {
                $scope.loading=true;
            });
        };
        $scope.stopContainer = function(container) {
            ContainerService.stopContainer(container).then(function() {
                $scope.loading=true;
            });
        };
    });

    index.controller('InstalledApplicationController', function($scope, InstalledApplicationService, vertxEventBusService) {
        var loadApplications = function() {
            InstalledApplicationService.getInstalledWebApplications().success(function(data) {
                $scope.applications = data;
                $scope.loading=false;
            });
        };
        loadApplications();
        $scope.startApplication = function(application) {
            InstalledApplicationService.startApplication(application).then(function() {
                $scope.loading=true;
            });
        };
        $scope.stopApplication = function(application) {
            InstalledApplicationService.stopApplication(application).then(function() {
                $scope.loading=true;
            });
        };
        $scope.deleteApplication = function(application) {
            InstalledApplicationService.deleteApplication(application).then(function() {
                $scope.loading=true;
            });
        };
        vertxEventBusService.on('WebAppContextStatusChanged', loadApplications);
        vertxEventBusService.on('ContainerStatusChanged', loadApplications);
    });

    index.controller('MessageBarController', function($scope, vertxEventBusService) {
        $scope.message = 'main.header.description';
        vertxEventBusService.on('StatusBarMessage', function(event) {
            $scope.message = event.value;
        });
    });

})();
