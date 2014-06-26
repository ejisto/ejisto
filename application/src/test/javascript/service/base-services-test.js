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
     * Created by celestino on 25/06/2014.
     */
    describe("Base services", function () {

        beforeEach(module("BaseServices"));

        describe("FieldService", function() {
            var service;
            beforeEach(inject(function($rootScope, $httpBackend, $controller, FieldService) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                service = FieldService;
            }));

            it("should be defined", function () {
                expect(service).not.toBeUndefined();
            });

            it("should handle getAllFields request", function () {
                httpBackend.expectGET('/fields/all').respond(200, {}, {}, "OK");
                service.getAllFields();
            });

            it("should handle getFieldsGrouped request", function () {
                httpBackend.expectGET('/fields/grouped').respond(200, {}, {}, "OK");
                service.getFieldsGrouped();
            });

            it("should handle getFieldsByContextPath request", function () {
                httpBackend.expect('GET','/fields/by-context-path?contextPath=test').respond(200, {}, {}, "OK");
                service.getFieldsByContextPath("test");
            });

            it("should handle updateField request", function () {
                httpBackend.expect('PUT','/field/update?contextPath=a&fieldClassName=b&fieldName=c&newValue=newValue').respond(200, {}, {}, "OK");
                service.updateField({
                    'contextPath': 'a',
                    'className': 'b',
                    'fieldName': 'c'
                },"newValue");
            });

            it("should handle validateField request", function () {
                httpBackend.expect('PUT','/field/validate/for/1?contextPath=a&fieldClassName=b&fieldName=c&newValue=newValue').respond(200, {}, {}, "OK");
                service.validateField({
                    'contextPath': 'a',
                    'className': 'b',
                    'fieldName': 'c'
                },"newValue", "1");
            });

            it("should handle activateFields request", function () {
                httpBackend.expect('PUT','/fields/bulk/update').respond(200, {}, {}, "OK");
                service.activateFields([{
                    'contextPath': 'a',
                    'className': 'b',
                    'fieldName': 'c'
                }]);
            });

            it("should handle createNewField request", function () {
                httpBackend.expect('POST','/field/new?contextPath=a&fieldClassName=b&fieldName=c&fieldType=type&fieldValue=value').respond(200, {}, {}, "OK");
                service.createNewField({
                    'contextPath': 'a',
                    'className': 'b',
                    'name': 'c',
                    'type': 'type',
                    'value': 'value'
                });
            });

            afterEach(function() {
                httpBackend.verifyNoOutstandingExpectation();
            });

        });

        describe("InstallApplicationService", function() {
            var service;
            beforeEach(inject(function($rootScope, $httpBackend, $controller, InstallApplicationService) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                service = InstallApplicationService;
            }));

            it("should be defined", function () {
                expect(service).not.toBeUndefined();
            });

            it("should handle selectExternalLibraries request", function () {
                httpBackend.expect('PUT','/application/new/1/include?resources=a,b').respond(200, {}, {}, "OK");
                service.selectExternalLibraries(['a', 'b'], '1');
            });

            it("should handle publishApplication request", function () {
                httpBackend.expect('POST','/application/new/1/publish', [{name: '1st'}, {name:'2nd'}]).respond(200, {}, {}, "OK");
                service.publishApplication('1', [{
                    element: {
                        name: '1st'
                    }
                }, {
                    element: {
                        name: '2nd'
                    }
                }]);
            });

            afterEach(function() {
                httpBackend.verifyNoOutstandingExpectation();
            });

        });

        describe("ContainerService", function() {
            var service, eventBusService;
            beforeEach(inject(function($rootScope, $httpBackend, $controller, ContainerService, vertxEventBusService) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                service = ContainerService;
                spyOn(vertxEventBusService, 'send').andReturn({});
                eventBusService = vertxEventBusService;
            }));

            it("should be defined", function () {
                expect(service).not.toBeUndefined();
            });

            it("should handle getRegisteredContainers request", function () {
                httpBackend.expect('GET','/containers/list').respond(200, {}, {}, "OK");
                service.getRegisteredContainers();
            });

            it("should handle downloadAndInstall request", function () {
                httpBackend.expect('PUT', '/containers/install?cargoID=a&defaultContainer=true&url=b').respond(200, {}, {}, "OK");
                service.downloadAndInstall('a', 'b', true);
            });

            it("should handle loadSupportedContainerTypes request", function () {
                httpBackend.expect('GET', '/containers/supported').respond(200, {}, {}, "OK");
                service.loadSupportedContainerTypes();
            });

            it("should handle startContainer request", function () {
                service.startContainer({id:1});
                expect(eventBusService.send).toHaveBeenCalledWith('StartContainer', {'containerId': 1}, true);
            });

            it("should handle stopContainer request", function () {
                service.stopContainer({id:1});
                expect(eventBusService.send).toHaveBeenCalledWith('StopContainer', {'containerId': 1}, true);
            });

            afterEach(function() {
                httpBackend.verifyNoOutstandingExpectation();
            });

        });

        describe("InstalledApplicationService", function() {
            var service, eventBusService;
            beforeEach(inject(function($rootScope, $httpBackend, $controller, InstalledApplicationService, vertxEventBusService) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                service = InstalledApplicationService;
                spyOn(vertxEventBusService, 'send').andReturn({});
                eventBusService = vertxEventBusService;
            }));

            it("should be defined", function () {
                expect(service).not.toBeUndefined();
            });

            it("should handle getInstalledWebApplications request", function () {
                httpBackend.expect('GET','/webApplications/list').respond(200, {}, {}, "OK");
                service.getInstalledWebApplications();
            });

            it("should handle startApplication request", function () {
                service.startApplication({contextPath:'/', containerId:'a'});
                expect(eventBusService.send).toHaveBeenCalledWith('StartApplication', {'contextPath': '/', containerId:'a'}, true);
            });

            it("should handle stopApplication request", function () {
                service.stopApplication({contextPath:'/', containerId:'a'});
                expect(eventBusService.send).toHaveBeenCalledWith('StopApplication', {'contextPath': '/', containerId:'a'}, true);
            });

            it("should handle deleteApplication request", function () {
                service.deleteApplication({contextPath:'/', containerId:'a'});
                expect(eventBusService.send).toHaveBeenCalledWith('DeleteApplication', {'contextPath': '/', containerId:'a'}, true);
            });

            afterEach(function() {
                httpBackend.verifyNoOutstandingExpectation();
            });

        });

        describe("HttpErrorHandler", function() {
            var service;
            beforeEach(inject(function($rootScope, $httpBackend, $controller, HttpErrorHandler) {
                initGlobalVariables($rootScope, $httpBackend, $controller);
                service = HttpErrorHandler;
            }));

            it("should be defined", function () {
                expect(service).not.toBeUndefined();
            });

            it("should handle http errors", function () {
                spyOn(rootScope, '$broadcast').andReturn({});
                service.handle({message: 'this is a test'});
                expect(rootScope.$broadcast).toHaveBeenCalledWith('applicationError', 'this is a test');
            });
        });

    });
})();