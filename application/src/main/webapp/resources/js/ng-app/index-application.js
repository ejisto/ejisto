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

    var index = angular.module('indexApplication', ['ui.bootstrap',
        'pascalprecht.translate', 'FieldEditor',
        'ContainerManager', 'BaseServices',
        'WebApplicationManager', 'knalli.angular-vertxbus',
        'Utilities', 'angularFileUpload', 'xeditable'
    ]);

    index.run(function(editableOptions, editableThemes) {
        editableThemes.bs3.submitTpl = '<button type="submit" class="btn btn-primary"><span class="fa fa-check"></span></button>';
        editableThemes.bs3.cancelTpl = '<button type="button" class="btn btn-default" ng-click="$form.$cancel()">'+
                '<span class="fa fa-times"></span>'+
                '</button>';
        editableOptions.theme = 'bs3';
    });

    index.config(function ($translateProvider) {
        $translateProvider.useUrlLoader("/translations");
        $translateProvider.preferredLanguage('en');
    });

    Ejisto.controllers.index.install(index);
    Ejisto.controllers.applicationInstaller.install(index);

    index.controller('MenuController', function ($scope, $modal, $log) {
        $scope.installNewApplication = function() {
            var wizard = $modal.open({
                templateUrl:'/resources/templates/wizard/application-installer-index.html',
                backdrop: 'static',
                keyboard: false,
                controller: Ejisto.controllers.applicationInstaller.ApplicationInstaller
            });
            wizard.result.then(function() {
                $log.debug("closed");
            });
        };
    });

    index.controller('ErrorController', function($scope, $rootScope) {
        $rootScope.$on('applicationError', function(event, message) {
            $scope.errorMessage = message;
        });
    });

    index.controller('MessageBarController', function($scope, vertxEventBusService) {
        $scope.message = 'main.header.description';
        vertxEventBusService.on('StatusBarMessage', function(statusBarMessage) {
            $scope.message = statusBarMessage.message;
        });
    });

})();
