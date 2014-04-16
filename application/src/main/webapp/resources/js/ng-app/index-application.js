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
    /**
     * Created by celestino on 3/22/14.
     */
    var index = angular.module('indexApplication', ['ui.bootstrap', 'pascalprecht.translate', 'FieldEditor', 'ContainerManager', 'BaseServices', 'WebApplicationManager', 'knalli.angular-vertxbus']);

    index.config(function ($translateProvider) {
        $translateProvider.useUrlLoader("/translations");
        $translateProvider.preferredLanguage('en');
    });

    index.controller('HeaderController', function ($scope) {
        $scope.testMessage = "Hello, World!"
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
            });
        };
        loadContainers();
        vertxEventBusService.on('ContainerStatusChanged', loadContainers);
    });

    index.controller('InstalledApplicationController', function($scope, InstalledApplicationService, vertxEventBusService) {
        var loadApplications = function() {
            InstalledApplicationService.getInstalledWebApplications().success(function(data) {
                $scope.applications = data;
            });
        };
        loadApplications();
        vertxEventBusService.on('ChangeWebAppContextStatus', loadApplications);
        vertxEventBusService.on('ContainerStatusChanged', loadApplications);
    });

    index.controller('StatusBarController', function($scope, vertxEventBusService) {
        $scope.message = 'main.header.description';
        $scope.$on('vertx-eventbus.system.disconnected', function() {
            $scope.connected=false;
        });
        $scope.$on('vertx-eventbus.system.connected', function() {
            $scope.connected=true;
        });
        vertxEventBusService.on('StatusBarMessage', function(event) {
            $scope.message = event.value;
        });
    });


})();
