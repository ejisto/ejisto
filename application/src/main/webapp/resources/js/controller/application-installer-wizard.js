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

        ApplicationInstaller: function ($scope, $filter) {
            $scope.descriptor = {};
            $scope.progressIndicator = {};
            $scope.progressIndicator.loading = false;
            $scope.cancel = function () {
                if (confirm($filter('translate')('wizard.quit.message'))) {
                    $scope.$dismiss('canceled');
                }
            };
            $scope.processCompleted = function () {
                $scope.$close(true);
            };
        },
        WizardFileSelectionController: function($scope, vertxEventBusService, $upload) {

            $scope.onFileSelect = function ($files) {
                $scope.progressIndicator.loading = true;
                var firstFile = _.first(_.filter($files, function(file) {
                    return /.*?\.war/.test(file.name);
                }));
                if(firstFile) {
                    $scope.upload = $upload.upload({
                       url: '/application/new/upload',
                       method: 'PUT',
                       file: firstFile
                   }).success(function (data, status, headers, config) {
                       $scope.descriptor.sessionID = data.sessionID;
                       $scope.descriptor.includedJars = data.resources;
                       $scope.descriptor.fileName = firstFile.name;
                       $scope.progressIndicator.loading = false;
                       $scope.newUploadRequested = false;
                   }).error(function () {
                       $scope.progressIndicator.loading = false;
                   });
                }
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
                    $scope.progressIndicator.status = progressStatus.message;
                    $scope.extractionCompleted = true;
                }
            });
        },
        WizardLibFilterController: function($scope) {
            $scope.descriptor.selectedResources = [];
            $scope.addRemoveResource = function(resource, $event) {
                var list = $scope.descriptor.selectedResources;
                if(!$event.target.checked) {
                    _.remove(list, function(e) { return e == resource;});
                } else {
                    list.push(resource);
                }
                $scope.descriptor.selectedResources = list;
            };
        },
        WizardFieldEditorController: function($scope) {
            $scope.activateField = function(element, value) {
                element.active = true;
                return undefined;
            };
        },
        StepEvaluator: function($q, InstallApplicationService) {
            return {
                commitSelectFile: function (step, scope) {
                    var deferred = $q.defer();
                    var descriptor = scope.descriptor;
                    if(descriptor) {
                        deferred.resolve(descriptor);
                    } else {
                        deferred.reject("descriptor required");
                    }
                    return deferred.promise;
                },
                commitFilterClasses: function(step, scope) {
                    var deferred = $q.defer();
                    InstallApplicationService.selectExternalLibraries(scope.descriptor.selectedResources, scope.descriptor.sessionID).then(function(success) {
                        scope.descriptor.fields = success.data;
                        deferred.resolve("success");
                    }, function(error) {
                        scope.descriptor.fields = [];
                        deferred.reject(error);
                    });
                    return deferred.promise;
                }
            };
        },
        install: function(module) {

            module.service('StepEvaluator', this.StepEvaluator);
            module.controller('WizardFileSelectionController', this.WizardFileSelectionController);
            module.controller('WizardLibFilterController', this.WizardLibFilterController);
            module.controller('WizardFieldEditorController', this.WizardFieldEditorController);
            module.directive("wizardContainer", function(StepEvaluator) {
                var evaluateCurrentStepData = function(scope, root, steps, currentStep) {
                    scope.activeStep = currentStep;
                    scope.showPrevious = angular.isDefined(currentStep.previous);
                    scope.showNext = angular.isDefined(currentStep.next);
                    scope.showCancel = true;
                    scope.showFinish = !angular.isDefined(currentStep.next);
                    var element = root.find('#'+currentStep.id);
                    scope.stepTitle=element.attr('data-step-title');
                    element.addClass('activeStep');
                };
                var initSteps = function(steps) {
                    var previousSteps = _.clone(steps);
                    previousSteps.unshift(undefined);
                    var nextSteps = _.clone(steps);
                    nextSteps.shift();
                    return _.map(_.zip(steps, previousSteps, nextSteps), function(obj) {
                        return {
                            'id' : obj[0],
                            'previous': obj[1],
                            'next': obj[2]
                        }
                    }).filter(function(o) {
                        return o.id;
                    });
                };
                return {
                    restrict: 'A',
                    link: function(scope, element, attrs) {
                        var stepIds = _.map(element.find('.wizardStep'), function(el) {
                            return el.attributes.getNamedItem('id').value;
                        });
                        var steps = initSteps(stepIds);
                        evaluateCurrentStepData(scope, element, steps, _.first(steps));
                        scope.nextStep = function(currentStep) {
                            var fnName = "commit" + currentStep.id.charAt(0).toUpperCase() + currentStep.id.slice(1);
                            if(angular.isFunction(StepEvaluator[fnName])) {
                                scope.progressIndicator.loading=true;
                                StepEvaluator[fnName](currentStep, scope).then(function(result) {
                                    var nextStep = _.find(steps, {'id': currentStep.next});
                                    if(nextStep) {
                                        element.find('#'+currentStep.id).removeClass('activeStep');
                                        evaluateCurrentStepData(scope, element, steps, nextStep);
                                        scope.progressIndicator.loading=false;
                                    }
                                });
                            }
                        };
                        scope.previousStep = function(currentStep) {
                            var fnName = "clean" + currentStep.id.charAt(0).toUpperCase() + currentStep.id.slice(1);
                            var activatePreviousStep = function() {
                                var previousStep = _.find(steps, {'id': currentStep.previous});
                                if(previousStep) {
                                    element.find('#'+currentStep.id).removeClass('activeStep');
                                    evaluateCurrentStepData(scope, element, steps, previousStep);
                                }
                            };
                            if(angular.isFunction(StepEvaluator[fnName])) {
                                StepEvaluator[fnName](currentStep, scope).then(function(result) {
                                    activatePreviousStep();
                                });
                            } else {
                                activatePreviousStep();
                            }
                        };
                        scope.cancelProcess = function() {
                            scope.cancel();
                        }
                    }
                };
            });
        }
    };

})();