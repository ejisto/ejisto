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
            template: '<i class="fa btn btn-lg btn-link" data-ng-class="{\'fa-link\':connected, \'fa-unlink\':!connected}" data-tooltip="{{connectionStatusMessage | translate}}" data-tooltip-trigger="mouseenter"></i>',
            restrict: 'E',
            replace: true,
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
    utilities.directive("statusMessage", function(vertxEventBusService) {
        return {
            template: "<span>{{message}}</span>",
            restrict: 'E',
            replace: true,
            link: function(scope, element, attrs) {
                scope.message = '';
                vertxEventBusService.on('StatusBarMessage', function(event) {
                    scope.message = angular.filter('translate').apply(event.value);
                });
            }
        }
    });
})();