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
    var index = angular.module('indexApplication', ['ui.bootstrap', 'pascalprecht.translate', 'FieldEditor', 'ContainerManager', 'BaseServices']);

    index.config(function ($translateProvider) {
        $translateProvider.useUrlLoader("/translations");
        $translateProvider.preferredLanguage('en');
    });

    index.controller('HeaderController', function ($scope) {
        $scope.testMessage = "Hello, World!"
    });

    index.controller('PropertiesEditorController', function ($scope, FieldService) {
        $scope.selectedEditor = 'HIERARCHICAL';
        FieldService.getFieldsGrouped().then(function(result) {
            var collapsedStatusContainer = {
                status : {
                    paths : []
                },
                isCollapsed : function(path) {
                    return status[path] && status[path] === 'COLLAPSED';
                },
                toggleCollapse : function(path) {
                    var flag = status.paths[path];
                    if(!flag) {
                        flag = 'EXPANDED';
                    }
                    status.paths[path] = (flag === 'COLLAPSED' ? 'EXPANDED' : 'COLLAPSED');
                }
            };
            $scope.toggleCollapse = function(path) {
                collapsedStatusContainer.toggleCollapse(path);
            };
            $scope.isCollapsed = function(path) {
                return collapsedStatusContainer.isCollapsed(path);
            };
            if(!$scope.fieldContainer) {
                $scope.fieldContainer = {};
            }
            $scope.fieldContainer.fields = result.data;
        }, function(error) {
            $scope.$emit('error', error.message);
        });
    });

    index.controller('ErrorController', function($scope, $rootScope) {
        $rootScope.$on('error', function(message) {
            $scope.errorMessage = message;
        });
    });

})();
