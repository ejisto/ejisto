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
        'Utilities'
    ]);

    index.config(function ($translateProvider) {
        $translateProvider.useUrlLoader("/translations");
        $translateProvider.preferredLanguage('en');
    });

    var WizardController = function($scope, FieldService) {
        $scope.cancel = function() {
            if(confirm("cancel?")) {
                $scope.$dismiss('canceled');
            }
        };
        $scope.finished = function() {
            $scope.$close(true);
        };
        $scope.commitSelectFile = function(step) {
            return FieldService.getFieldsGrouped();
        };
    };

    index.controller('MenuController', function ($scope, $modal, $log) {
        $scope.installNewApplication = function() {
            var wizard = $modal.open({
                templateUrl:'/resources/templates/wizard/index.html',
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
