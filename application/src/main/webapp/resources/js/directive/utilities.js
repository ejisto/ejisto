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
    var utilities = angular.module('Utilities', ['ui.bootstrap', 'pascalprecht.translate', 'knalli.angular-vertxbus']);
    utilities.directive("systemConnectionStatus", function () {
        return {
            templateUrl: '/resources/templates/utilities/systemConnectionStatus.html',
            restrict: 'E',
            link: function (scope, element, attrs) {
                scope.$on('vertx-eventbus.system.disconnected', function () {
                    scope.connected = false;
                    scope.connectionStatusMessage = 'websocket.server.connection.lost';
                });
                scope.$on('vertx-eventbus.system.connected', function () {
                    scope.connected = true;
                    scope.connectionStatusMessage = 'websocket.server.connection.ok';
                });
            }
        };
    });
    utilities.directive("statusMessage", function(vertxEventBusService, $filter) {
        return {
            template: "<span>{{message}}</span>",
            restrict: 'E',
            replace: true,
            link: function(scope, element, attrs) {
                scope.message = $filter('translate')("main.header.description");
                vertxEventBusService.on('StatusBarMessage', function(event) {
                    scope.message = $filter('translate')(event.value);
                });
            }
        }
    });
    utilities.directive("wizardContainer", function() {
        var evaluateCurrentStepData = function(scope, root, steps, currentStep) {
            scope.activeStep = currentStep;
            scope.showPrevious = angular.isDefined(currentStep.previous);
            scope.showNext = angular.isDefined(currentStep.next);
            scope.showCancel = true;
            scope.showFinish = !angular.isDefined(currentStep.next);
            root.find('#'+currentStep.id).addClass('activeStep');
        };
        var initSteps = function(steps) {
            var previousSteps = _.clone(steps);
            previousSteps.unshift(null);
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
                    if(angular.isFunction(scope[fnName])) {
                        scope[fnName](currentStep).then(function(result) {
                            var nextStep = _.find(steps, {'id': currentStep.next});
                            if(nextStep) {
                                element.find('#'+currentStep.id).removeClass('activeStep');
                                evaluateCurrentStepData(scope, element, steps, nextStep);
                            }
                        });
                    }
                };
                scope.cancelProcess = function() {
                    scope.cancel();
                }
            }
        };
    });
})();