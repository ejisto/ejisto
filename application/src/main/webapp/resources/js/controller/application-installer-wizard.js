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

        ApplicationInstaller: function ($scope, $filter, InstallApplicationService) {
            $scope.descriptor = {};
            $scope.progressIndicator = {};
            $scope.progressIndicator.loading = false;
            $scope.cancel = function () {
                if (confirm($filter('translate')('wizard.quit.message'))) {
                    $scope.$dismiss('canceled');
                }
            };
            $scope.processCompleted = function () {
                InstallApplicationService.publishApplication($scope.descriptor.sessionID, $scope.descriptor.editedFields)
                        .then(function(success) {
                                $scope.$close(true);
                         });
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
        WizardFieldEditorController: function($scope, FieldService) {
            $scope.validateField = function(element, value) {
                return FieldService.validateField(element, value, $scope.descriptor.sessionID);
            };
            $scope.activateField = function(element, value) {
                var match = _.chain(Ejisto.controllers.applicationInstaller.flattenDescriptor($scope.descriptor.fieldContainer))
                 .flatten()
                 .find(function(e) {
                        var el = e.element;
                        return el.contextPath == element.contextPath &&
                               el.className == element.className &&
                               el.fieldName == element.fieldName;
                 }).value();
                if(match) {
                    match.active = true;
                }
            }
        },
        flattenDescriptor: function(container) {
            if(container.children && container.children.length > 0) {
                return _.map(container.children, Ejisto.controllers.applicationInstaller.flattenDescriptor);
            }
            return container;
        },
        WizardSummaryController: function($scope) {
            var flattenFields = _.flatten(Ejisto.controllers.applicationInstaller.flattenDescriptor($scope.descriptor.fieldContainer));
            $scope.descriptor.editedFields = _.filter(flattenFields, function(e) {
                return e.active === true;
            });
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
                        scope.descriptor.fieldContainer = success.data;
                        deferred.resolve("success");
                    }, function(error) {
                        scope.descriptor.fieldContainer = [];
                        deferred.reject(error);
                    });
                    return deferred.promise;
                },
                commitEditProperties: function(step, scope) {
                    var deferred = $q.defer();
                    //nothing to be done here
                    deferred.resolve(step);
                    return deferred.promise;
                }
            };
        },
        install: function(module) {

            module.service('StepEvaluator', this.StepEvaluator);
            module.controller('WizardFileSelectionController', this.WizardFileSelectionController);
            module.controller('WizardLibFilterController', this.WizardLibFilterController);
            module.controller('WizardFieldEditorController', this.WizardFieldEditorController);
            module.controller('WizardSummaryController', this.WizardSummaryController);
            module.directive("wizardContainer", function(StepEvaluator) {
                var evaluateCurrentStepData = function(scope, root, steps, currentStep) {
                    scope.activeStep = currentStep;
                    scope.currentStepId = currentStep.id;
                    scope.showPrevious = angular.isDefined(currentStep.previous);
                    scope.showNext = angular.isDefined(currentStep.next);
                    scope.showCancel = true;
                    scope.showFinish = !angular.isDefined(currentStep.next);
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
                        var stepIds = attrs.wizardContainer.split(",");
                        var steps = initSteps(stepIds);
                        evaluateCurrentStepData(scope, element, steps, _.first(steps));
                        scope.nextStep = function(currentStep) {
                            var fnName = "commit" + currentStep.id.charAt(0).toUpperCase() + currentStep.id.slice(1);
                            if(angular.isFunction(StepEvaluator[fnName])) {
                                scope.progressIndicator.loading=true;
                                StepEvaluator[fnName](currentStep, scope).then(function(result) {
                                    var nextStep = _.find(steps, {'id': currentStep.next});
                                    if(nextStep) {
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
                        };
                        scope.$on('stepTitleChanged', function(event, title) {
                            scope.stepTitle = title;
                        });
                    }
                };
            });
            module.directive("wizardStepTitle", function() {
                return {
                    restrict: 'A',
                    link: function(scope, element, attrs) {
                        scope.$emit('stepTitleChanged', attrs.wizardStepTitle);
                    }
                }
            });
        }
    };

})();