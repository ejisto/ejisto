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

var Ejisto = Ejisto || {};
Ejisto.controllers = Ejisto.controllers || {};

(function () {
    "use strict";
    Ejisto.controllers.index = {
        PropertiesEditorController: function ($scope, FieldService) {
            $scope.selectedEditor = 'HIERARCHICAL';
            FieldService.getFieldsGrouped().then(function(result) {
                $scope.fields = result.data;
            });
            $scope.updateField = function(element, data) {
                return FieldService.updateField(element, data);
            };
        },
        ContainersController: function($scope, ContainerService, vertxEventBusService) {
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
        },
        InstalledApplicationsController: function($scope, InstalledApplicationService, vertxEventBusService) {
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
            vertxEventBusService.on('ApplicationDeployed', loadApplications);
            vertxEventBusService.on('ContainerStatusChanged', loadApplications);
            vertxEventBusService.on('ApplicationInstallFinalization', function() {
                $scope.loading = true;
            });
        },
        install: function(module) {

            module.controller('PropertiesEditorController', this.PropertiesEditorController);
            module.controller('ContainersController', this.ContainersController);
            module.controller('InstalledApplicationController', this.InstalledApplicationsController);
        }
    };
})();