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
        PropertiesEditorController: function ($scope, FieldService, vertxEventBusService) {
            $scope.selectedEditor = 'HIERARCHICAL';
            $scope.loading = true;
            var loadFields = function() {
                $scope.loading = true;
                FieldService.getFieldsGrouped().then(function(result) {
                    $scope.fields = result.data;
                    $scope.loading = false;
                });
            };
            loadFields();
            $scope.updateField = function(element, data) {
                return FieldService.updateField(element, data);
            };
            vertxEventBusService.on('WebAppContextStatusChanged', loadFields);
            vertxEventBusService.on('ApplicationDeployed', loadFields);
            vertxEventBusService.on('ApplicationInstallFinalization', function() {
                $scope.loading = true;
            });
            $scope.$on('reloadFields', loadFields);
        },
        ContainersController: function($scope, ContainerService, vertxEventBusService, $modal, $log) {
            var loadContainers = function() {
                ContainerService.getRegisteredContainers().success(function(data) {

                    if(data.length > 0) {
                        $scope.containers = data;
                    } else {
                        var wizard = $modal.open({
                             templateUrl:'/resources/templates/wizard/container-installer-index.html',
                             backdrop: 'static',
                             controller: Ejisto.controllers.index.DownloadDefaultContainerController
                        });
                        wizard.result.then(function() {
                            loadContainers();
                        });
                    }
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
        DownloadDefaultContainerController: function($scope, ContainerService, $log) {
            $scope.loading = false;
            $scope.containerDownloadData = {};
            ContainerService.loadSupportedContainerTypes().success(function(list) {
                $scope.containerTypes = list;
                if(list.length == 1) {
                    $scope.containerDownloadData.type = list[0];
                }
            });
            $scope.download = function() {
                $scope.loading = true;
                var downloadData = $scope.containerDownloadData;
                ContainerService.downloadAndInstall(downloadData.type.cargoID, downloadData.url, true).then(function(result) {
                            $scope.$close(true);
                        }, function(error) {
                            switch(error.status) {
                                case 400:
                                    $scope.urlRequested = false;
                                    $scope.$dismiss('canceled');
                                    break;
                                case 404:
                                    $scope.urlRequested = true;
                                    break;
                            }
                            $log.error(error);
                            $scope.loading = false;
                        });
            }
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