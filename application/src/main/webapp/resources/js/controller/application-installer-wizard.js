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
    Ejisto.controllers.applicationInstaller = {

        ApplicationInstaller: function ($scope) {
            $scope.descriptor = {};
            $scope.cancel = function () {
                if (confirm("cancel?")) {
                    $scope.$dismiss('canceled');
                }
            };
            $scope.finished = function () {
                $scope.$close(true);
            };
        },
        WizardFileSelectionController: function($scope, vertxEventBusService, $upload) {

            $scope.commitSelectFile = function (step) {
                var descriptor = $scope.descriptor;
                return FieldService.getFieldsGrouped();
            };

            $scope.onFileSelect = function ($files) {
                $scope.loading = true;
                _.forEach($files, function (file) {
                    $scope.upload = $upload.upload({
                       url: '/application/new/upload',
                       method: 'PUT',
                       file: file
                   }).success(function (data, status, headers, config) {
                       $scope.sessionID = data.sessionID;
                       $scope.includedJars = data.resources;
                       $scope.fileName = file.name;
                       $scope.loading = false;
                       $scope.newUploadRequested = false;
                   }).error(function () {
                       $scope.loading = false;
                   });
                });
            };
            $scope.requestNewUpload = function () {
                $scope.newUploadRequested = true;
                $scope.extractionCompleted = false;
            };
            vertxEventBusService.on('StartFileExtraction', function () {
                $scope.fileExtractionInProgress = true;
            });
            vertxEventBusService.on('FileExtractionProgress', function (progress) {
                var progressStatus = JSON.parse(progress);
                if (progressStatus.taskCompleted) {
                    $scope.fileExtractionInProgress = false;
                } else {
                    $scope.progressStatus = progressStatus.message;
                    $scope.extractionCompleted = true;
                }
            });
        },
        install: function(module) {

            module.controller('WizardFileSelectionController', this.WizardFileSelectionController);
        }
    };

})();