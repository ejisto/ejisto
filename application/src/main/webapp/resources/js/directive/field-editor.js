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

    var fieldEditor = angular.module('FieldEditor', ['ui.bootstrap', 'pascalprecht.translate', 'sf.treeRepeat']);

    fieldEditor.directive("fieldEditorChooser", function() {
        return {
            templateUrl: '/resources/templates/editor/chooser.html',
            restrict: 'E',
            link: function(scope, element, attrs) {
                scope.selectedEditor = "HIERARCHICAL";
            }
        };
    });

    fieldEditor.directive("flattenFieldEditor", function() {
        return {
            templateUrl: '/resources/templates/editor/flatten.html',
            restrict: 'E',
            link: function(scope, element, attrs) {

            }
        };
    });

    fieldEditor.directive("hierarchicalFieldEditor", function($q, $modal, $rootScope) {
        return {
            scope: {
                fields: '=',
                beforeUpdate: '=',
                afterUpdate: '=',
                profile: '@'
            },
            templateUrl: '/resources/templates/editor/hierarchical.html',
            restrict: 'E',
            link: function(scope, element, attrs) {
                var callFunction = function(f, el, $data) {
                    if(angular.isFunction(f)) {
                        var deferred = $q.defer();
                        var promise = f(el, $data);
                        if(promise && angular.isFunction(promise.then)) {
                            promise.then(function(success) {
                                deferred.resolve(success);
                            }, function(error) {
                                deferred.reject(error);
                            });
                        } else {
                            deferred.resolve(promise);
                        }
                        return deferred.promise;
                    }
                    return undefined;
                };
                scope.isExpanded = function(node) {
                    return node.expanded;
                };
                var hasChildren = function(node) {
                    return node && node.children && node.children.length > 0;
                };
                scope.getCurrentTooltipText = function(node) {
                    return node.expanded ? 'collapse.node.text': 'expand.node.text';
                };
                scope.hasChildren = function(node) {
                    return hasChildren(node);
                };
                scope.toggleExpandCollapse = function(node) {
                    if(hasChildren(node)) {
                        node.expanded = !node.expanded;
                    }
                };
                scope.getExpandCollapseIconClass = function(node) {
                    return node && node.expanded ? 'fa-level-up' : 'fa-level-down';
                };
                var flattenParentNodes = function(node) {
                    var elements = [];
                    if(node.children && node.children.length > 0) {
                        elements.push(node);
                        return elements.concat(_.map(node.children, flattenParentNodes));
                    }
                    return elements;
                };
                scope.expandCollapseChildren = function(node) {
                    var expand = !node.expanded;
                    _.flatten(flattenParentNodes(node)).filter(function(n) {
                        return n.children && n.children.length > 0;
                    }).forEach(function(child) {
                        child.expanded = expand;
                    });
                };
                var CreateNewFieldController = function($scope, InstalledApplicationService, FieldService) {
                    $scope.cancel = function() {
                        $scope.$dismiss("canceled");
                    };
                    InstalledApplicationService.getInstalledWebApplications().success(function(data) {
                        $scope.webApplications = _.map(data, 'contextPath');
                    });
                    $scope.field = {};
                    $scope.$watch('field.contextPath', function(newValue) {
                        if(!newValue) {
                            return;
                        }
                        FieldService.getFieldsByContextPath(newValue).success(function(data) {
                            $scope.classNames = _.uniq(_.map(data, 'className'));
                        });
                    });
                    $scope.registerField = function(field) {
                        FieldService.createNewField(field).success(function() {
                            $scope.$close(true);
                        });
                    };
                };
                var AddExistingFieldController = function($scope, FieldService) {
                    $scope.selectedCount = 0;
                    $scope.cancel = function() {
                        $scope.$dismiss("canceled");
                    };
                    FieldService.getAllFields().success(function(data) {
                        $scope.fields = data;
                    });
                    $scope.registerFields = function(fields) {
                        var toBeActivated = _.filter(fields, function(f) {
                            return f.active;
                        });
                        FieldService.activateFields(toBeActivated).success(function() {
                            $scope.$close(true);
                        });
                    };
                    $scope.toggleSelectedCount = function(field) {
                        var count = $scope.selectedCount;
                        count += (field.active ? -1 : 1);
                        $scope.selectedCount = Math.max(0, count);
                    };
                };
                scope.createNewField = function(parentNode) {
                    var w = $modal.open({
                        templateUrl:'/resources/templates/editor/createNewField.html',
                        backdrop: 'static',
                        controller: CreateNewFieldController
                    });
                    w.result.then(function() {
                        $rootScope.$broadcast('reloadFields', true);
                    });
                };
                scope.addExistingField = function(parentNode) {
                    var w = $modal.open({
                        templateUrl:'/resources/templates/editor/addExistingField.html',
                        backdrop: 'static',
                        size: 'lg',
                        controller: AddExistingFieldController
                    });
                    w.result.then(function() {
                        $rootScope.$broadcast('reloadFields', true);
                    });
                };
                scope.updateField = function(el, $data) {
                    return callFunction(scope.beforeUpdate, el, $data);
                };
                scope.afterUpdateField = function(el, $data) {
                    return callFunction(scope.afterUpdate, el, $data);
                };
            }
        };
    });
    fieldEditor.directive("ctrlButtons", function() {
        return {
            templateUrl:'/resources/templates/editor/ctrlButtons.html',
            restrict: 'E',
            link: function(scope, element, attrs) {
                scope.status = {
                    isOpen: false
                };
                scope.createNewFieldEnabled = attrs.profile == 'main';
                scope.addExistingFieldEnabled = attrs.profile == 'main';
            }
        };
    });

})();
