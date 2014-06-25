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
     * Created by celestino on 18/06/2014.
     */

    describe("Index Application", function () {

        beforeEach(module("indexApplication"));

        describe("MenuController", function() {
            var MenuCtrl;
            beforeEach(inject(function($rootScope, $httpBackend, $controller) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                MenuCtrl = loadController('MenuController');
            }));

            it("should be defined", function () {
                expect(MenuCtrl).not.toBeUndefined();
            });

            it("should define the menu actions", function() {
                expect(angular.isFunction(scope.installNewApplication)).toBeTruthy();
            });

        });

        describe("ErrorController", function() {
            var ErrorCtrl;
            beforeEach(inject(function($rootScope, $httpBackend, $controller) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                ErrorCtrl = loadController('ErrorController');
            }));

            it("should be defined", function () {
                expect(ErrorCtrl).not.toBeUndefined();
            });

            it("should handle error events", function() {
                rootScope.$broadcast("applicationError", "test");
                expect(scope.errorMessage).toEqual("test");
            });

        });

        describe("MessageBarController", function() {
            var MessageBarCtrl;

            beforeEach(inject(function($rootScope, $httpBackend, $controller) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                MessageBarCtrl = loadController('MessageBarController', {'vertxEventBusService': vertxEventBusService});
            }));

            it("should be defined", function () {
                expect(MessageBarCtrl).not.toBeUndefined();
            });

            it("should handle messages", function() {
                expect(scope.message).toEqual('main.header.description');
                eventHandler['StatusBarMessage']({message:"test"});
                expect(scope.message).toEqual('test');
            });

        });

        describe("PropertiesEditorController", function() {
            var PropertiesEditorCtrl;
            beforeEach(inject(function($rootScope, $httpBackend, $controller) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                PropertiesEditorCtrl = loadController('PropertiesEditorController', {
                    'vertxEventBusService': vertxEventBusService
                });
                httpBackend.expectGET("/translations?lang=en").respond(200, {}, {}, "OK");
            }));

            it("should be defined", function() {
                expect(PropertiesEditorCtrl).not.toBeUndefined();
            });

            it("should load all the fields", function() {
                expect(scope.selectedEditor).toEqual('HIERARCHICAL');
                expect(angular.isFunction(scope.updateField)).toBeTruthy();
                httpBackend.expectGET("/fields/grouped").respond(200, {}, {}, "OK");
                scope.$digest();
                httpBackend.expectGET("/fields/grouped").respond(200, {}, {}, "OK");
                expect(angular.isFunction(eventHandler['WebAppContextStatusChanged'])).toBeTruthy();
                eventHandler['WebAppContextStatusChanged']();
                httpBackend.expectGET("/fields/grouped").respond(200, {}, {}, "OK");
                expect(angular.isFunction(eventHandler['ApplicationDeployed'])).toBeTruthy();
                eventHandler['ApplicationDeployed']();
                expect(angular.isFunction(eventHandler['ApplicationInstallFinalization'])).toBeTruthy();
                eventHandler['ApplicationInstallFinalization']();
                expect(scope.loading).toBeTruthy();
            });
        });

        describe("ContainersController", function() {
            var ContainersCtrl, modal,
                    ContainersService, q,
                    getRegisteredContainers,httpPromise,
                    startContainer, stopContainer;
            beforeEach(inject(function($rootScope, $httpBackend, $controller, ContainerService, $q) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                q = $q;
                ContainersService = ContainerService;
                modal = jasmine.createSpyObj('$modal', ['open']);
                var deferred = q.defer();
                deferred.resolve({});
                httpPromise = jasmine.createSpyObj('httpPromise', ['success', 'error', 'then']);
                getRegisteredContainers = spyOn(ContainersService, 'getRegisteredContainers').andReturn(httpPromise);
                startContainer = spyOn(ContainersService, 'startContainer').andReturn(httpPromise);
                stopContainer = spyOn(ContainersService, 'stopContainer').andReturn(httpPromise);
                //ContainersService = jasmine.createSpyObj('ContainersService', ['getRegisteredContainers', 'startContainer', 'stopContainer']);
                ContainersCtrl = loadController('ContainersController', {
                    'vertxEventBusService': vertxEventBusService,
                    'ContainerService': ContainersService,
                    '$modal': modal
                });
                httpBackend.expectGET("/translations?lang=en").respond(200, {}, {}, "OK");
            }));

            it("should be defined", function() {
                expect(ContainersCtrl).not.toBeUndefined();
            });

            it("should populate the scope with control functions", function() {
                expect(angular.isFunction(scope.startContainer)).toBeTruthy();
                expect(angular.isFunction(scope.stopContainer)).toBeTruthy();
            });

            it("should load the registered containers", function() {
                expect(getRegisteredContainers.callCount).toEqual(1);
                expect(httpPromise.success).toHaveBeenCalled();
            });

            it("should handle the start/stop button properly", function() {
                var container = {id:'001'};
                scope.startContainer(container);
                expect(startContainer).toHaveBeenCalledWith(container);
                scope.stopContainer(container);
                expect(stopContainer).toHaveBeenCalledWith(container);

            });
        });


        describe("DownloadDefaultContainerController", function() {
            var ctrl, ContainersService, q,
                loadSupportedContainerTypes, httpPromise;
            beforeEach(inject(function($rootScope, $httpBackend, $controller, ContainerService, $q, $log) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                q = $q;
                ContainersService = ContainerService;
                var deferred = q.defer();
                deferred.resolve({});
                httpPromise = jasmine.createSpyObj('httpPromise', ['success', 'error', 'then']);
                loadSupportedContainerTypes = spyOn(ContainersService, 'loadSupportedContainerTypes').andReturn(httpPromise);
                scope.$close = function() {};
                scope.$dismiss = function() {};
                spyOn(scope, '$close');
                spyOn(scope, '$dismiss');
                ctrl = Ejisto.controllers.index.DownloadDefaultContainerController(scope, ContainersService, $log);
            }));

            it("should define the download function", function() {
                expect(angular.isFunction(scope.download)).toBeTruthy();
            });

            it("should load the supported container types", function() {
                expect(loadSupportedContainerTypes.callCount).toEqual(1);
                expect(httpPromise.success).toHaveBeenCalled();
            });

            it("should handle a successful download", function() {
                spyOn(ContainersService, 'downloadAndInstall').andReturn(successPromise);
                scope.containerDownloadData = {
                    url: 'aaa',
                    type: {
                        cargoID: 'xxx'
                    }
                };
                scope.download();
                expect(ContainersService.downloadAndInstall).toHaveBeenCalledWith('xxx', 'aaa', true);
                expect(scope.$close).toHaveBeenCalledWith(true);
            });

            it("should handle a failed (404) download", function() {
                spyOn(ContainersService, 'downloadAndInstall').andReturn(errorPromise(404));
                scope.containerDownloadData = {
                    url: 'aaa',
                    type: {
                        cargoID: 'xxx'
                    }
                };
                scope.download();
                expect(ContainersService.downloadAndInstall).toHaveBeenCalledWith('xxx', 'aaa', true);
                expect(scope.urlRequested).toBeTruthy();
                expect(scope.$close).not.toHaveBeenCalled();
                expect(scope.$dismiss).not.toHaveBeenCalled();
            });

            it("should handle a canceled download", function() {
                spyOn(ContainersService, 'downloadAndInstall').andReturn(errorPromise(400));
                scope.containerDownloadData = {
                    url: 'aaa',
                    type: {
                        cargoID: 'xxx'
                    }
                };
                scope.download();
                expect(ContainersService.downloadAndInstall).toHaveBeenCalledWith('xxx', 'aaa', true);
                expect(scope.urlRequested).not.toBeTruthy();
                expect(scope.$close).not.toHaveBeenCalled();
                expect(scope.$dismiss).toHaveBeenCalledWith('canceled');
            });

        });

        describe("InstalledApplicationsController", function() {
            var ctrl, installedApplicationService, application, httpPromise;
            beforeEach(inject(function($rootScope, $httpBackend, $controller, InstalledApplicationService) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                installedApplicationService = InstalledApplicationService;
                httpPromise = jasmine.createSpyObj('httpPromise', ['success', 'error', 'then']);
                spyOn(installedApplicationService, 'getInstalledWebApplications').andReturn(httpPromise);
                spyOn(installedApplicationService, 'startApplication').andReturn(httpPromise);
                spyOn(installedApplicationService, 'stopApplication').andReturn(httpPromise);
                spyOn(installedApplicationService, 'deleteApplication').andReturn(httpPromise);
                ctrl = loadController('InstalledApplicationsController', {
                    'vertxEventBusService': vertxEventBusService,
                    'InstalledApplicationService': installedApplicationService
                });
                application = {contextPath : "/ejisto-test", containerId:'001'};
            }));

            it("should be defined", function() {
                expect(ctrl).not.toBeUndefined();
            });

            it("should load all the applications at startup", function() {
                expect(installedApplicationService.getInstalledWebApplications).toHaveBeenCalled();
            });

            it("should handle the 'startApplication' request", function() {
                scope.startApplication(application);
                expect(installedApplicationService.startApplication).toHaveBeenCalledWith(application);
            });

            it("should handle the 'stopApplication' request", function() {
                scope.stopApplication(application);
                expect(installedApplicationService.stopApplication).toHaveBeenCalledWith(application);
            });

            it("should handle the 'deleteApplication' request", function() {
                scope.deleteApplication(application);
                expect(installedApplicationService.deleteApplication).toHaveBeenCalledWith(application);
            });
        });

    });


})();