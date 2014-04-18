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
     * Created by celestino on 26/03/2014.
     */
    var baseServices = angular.module('BaseServices', ['ui.bootstrap', 'pascalprecht.translate']);

    baseServices.service("FieldService", function($http, HttpErrorHandler) {
        return {
            getFieldsGrouped : function() {
                return $http.get('/fields/grouped').error(HttpErrorHandler.handle);
            }
        };
    });

    baseServices.service("ContainerService", function($http, vertxEventBusService, HttpErrorHandler) {
        return {
            getRegisteredContainers : function() {
                return $http.get('/containers/list').error(HttpErrorHandler.handle);
            },
            startContainer: function(container) {
                return vertxEventBusService.send('StartContainer', {"containerId" : container.id}, true);
            },
            stopContainer: function(container) {
                return vertxEventBusService.send('StopContainer', {"containerId" : container.id}, true);
            }
        };
    });

    baseServices.service("InstalledApplicationService", function($http, HttpErrorHandler) {
        return {
            getInstalledWebApplications : function() {
                return $http.get('/webApplications/list').error(HttpErrorHandler.handle);
            }
        };
    });

    baseServices.service("HttpErrorHandler", function($rootScope, $log) {
        return {
            handle : function(error) {
                $log.warn(error);
                $rootScope.$broadcast('applicationError', error.message);
            }
        };
    });

})();