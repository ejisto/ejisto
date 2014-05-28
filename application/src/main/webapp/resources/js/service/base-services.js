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
    var baseServices = angular.module('BaseServices', ['ui.bootstrap', 'knalli.angular-vertxbus', 'pascalprecht.translate']);

    baseServices.service("FieldService", function($http, HttpErrorHandler) {
        var callValidation = function(field, value, url) {
            return $http['put'](url, null, {
                params: {
                    'contextPath': field.contextPath,
                    'className': field.className,
                    'fieldName': field.fieldName,
                    'newValue': value
                }
            }).error(HttpErrorHandler.handle);
        };
        return {
            getFieldsGrouped : function() {
                return $http.get('/fields/grouped').error(HttpErrorHandler.handle);
            },
            updateField: function(field, value) {
                return callValidation(field, value, '/field/update');
            },
            validateField: function(field, value) {
                return callValidation(field, value, '/field/validate');
            }
        };
    });

    baseServices.service("InstallApplicationService", function($http, HttpErrorHandler) {
        return {
            selectExternalLibraries : function(libs, sessionId) {
                return $http['put']('/application/new/'+sessionId+'/include', null, {
                    params: {
                        resources: libs.join(",")
                    }
                }).error(HttpErrorHandler.handle);
            },
            publishApplication: function(sessionID, editedFields) {
                var fieldsToPublish = _.map(editedFields, function(field) {
                    return field.element;
                });
                return $http.post('/application/new/'+sessionID+'/publish', fieldsToPublish).error(HttpErrorHandler.handle);
            },
            updateField: function(field, value) {
                return $http['put']('/field/update', null, {
                    params: {
                        'contextPath': field.contextPath,
                        'className': field.className,
                        'fieldName': field.fieldName,
                        'newValue': value
                    }
                }).error(HttpErrorHandler.handle);
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
            },
            downloadAndInstall: function(cargoID, url, defaultContainer) {
                return $http['put']('/containers/install', null, {
                    params: {
                        "cargoID": cargoID,
                        "url": url,
                        "defaultContainer": defaultContainer
                    }
                });
            },
            loadSupportedContainerTypes: function() {
                return $http.get('/containers/supported').error(HttpErrorHandler.handle);
            }
        };
    });

    baseServices.service("InstalledApplicationService", function($http, HttpErrorHandler, vertxEventBusService) {
        return {
            getInstalledWebApplications : function() {
                return $http.get('/webApplications/list').error(HttpErrorHandler.handle);
            },
            startApplication : function(application) {
                return vertxEventBusService.send('StartApplication', {"contextPath" : application.contextPath, "containerId":application.containerId}, true);
            },
            stopApplication : function(application) {
                return vertxEventBusService.send('StopApplication', {"contextPath" : application.contextPath, "containerId":application.containerId}, true);
            },
            deleteApplication : function(application) {
                return vertxEventBusService.send('DeleteApplication', {"contextPath" : application.contextPath, "containerId":application.containerId}, true);
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