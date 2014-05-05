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
            $scope.loading = false;
            $scope.cancel = function () {
                if (confirm("cancel?")) {
                    $scope.$dismiss('canceled');
                }
            };
            $scope.processCompleted = function () {
                $scope.$close(true);
            };
        },
        WizardFileSelectionController: function($scope, vertxEventBusService, $upload) {

            $scope.onFileSelect = function ($files) {
                $scope.loading = true;
                _.forEach($files, function (file) {
                    $scope.upload = $upload.upload({
                       url: '/application/new/upload',
                       method: 'PUT',
                       file: file
                   }).success(function (data, status, headers, config) {
                        $scope.sessionID = data.sessionID;
                        $scope.descriptor.includedJars = data.resources;
                        $scope.descriptor.fileName = file.name;
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
        WizardLibFilterController: function($scope) {
            $scope.commitFilterClasses = function(step) {

            };
        },
        StepEvaluator: function($q) {
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

                }
            };
        },
        install: function(module) {

            module.service('StepEvaluator', this.StepEvaluator);
            module.controller('WizardFileSelectionController', this.WizardFileSelectionController);
            module.controller('WizardLibFilterController', this.WizardLibFilterController);
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
                                StepEvaluator[fnName](currentStep, scope).then(function(result) {
                                    var nextStep = _.find(steps, {'id': currentStep.next});
                                    if(nextStep) {
                                        element.find('#'+currentStep.id).removeClass('activeStep');
                                        evaluateCurrentStepData(scope, element, steps, nextStep);
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