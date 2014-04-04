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
     * Created by celestino on 28/03/2014.
     */
    var containerManager = angular.module('ContainerManager', ['ui.bootstrap', 'pascalprecht.translate']);

    containerManager.directive("containerManager", function() {
        return {
            templateUrl: '/resources/templates/container/containerTabs.html',
            restrict: 'E',
            link: function(scope, element, attrs) {
            }
        };
    });

    containerManager.directive("container", function() {
        return {
            templateUrl: '/resources/templates/container/container.html',
            restrict: 'E',
            link: function(scope, element, attrs) {
            }
        };
    });

})();