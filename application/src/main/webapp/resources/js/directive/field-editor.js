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
     * Created by celestino on 3/26/14.
     */
    var fieldEditor = angular.module('FieldEditor', ['ui.bootstrap', 'pascalprecht.translate', 'sf.treeRepeat', 'support']);

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

    fieldEditor.directive("hierarchicalFieldEditor", function() {
        return {
            templateUrl: '/resources/templates/editor/hierarchical.html',
            restrict: 'E',
            link: function(scope, element, attrs) {

            }
        };
    });

})();
