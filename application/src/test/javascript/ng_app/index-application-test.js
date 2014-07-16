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

        describe("Index Sections", function() {
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

        describe("Application installer Wizard",function() {
            describe("ApplicationInstaller", function() {
                var ctrl, installApplicationService, windowObj;
                beforeEach(inject(function($rootScope, $httpBackend, $controller, $filter, InstallApplicationService, $window) {
                    initGlobalVariables($rootScope, $httpBackend, $controller);
                    installApplicationService = InstallApplicationService;
                    windowObj = $window;
                    ctrl = Ejisto.controllers.applicationInstaller.ApplicationInstaller;
                    ctrl(scope, $filter, installApplicationService, windowObj);
                    scope.$dismiss = function() {};
                    scope.$close = function() {};
                }));

                it("should be defined", function() {
                    expect(ctrl).not.toBeUndefined();
                });

                it("should populate the scope with initial objects", function() {
                    expect(scope.descriptor).not.toBeUndefined();
                    expect(scope.progressIndicator).not.toBeUndefined();
                    expect(scope.progressIndicator.loading).not.toBeTruthy();
                    expect(angular.isFunction(scope.cancel)).toBeTruthy();
                    expect(angular.isFunction(scope.processCompleted)).toBeTruthy();
                });

                it("should handle the cancel request, when confirmed", function() {
                    spyOn(windowObj, 'confirm').andReturn(true);
                    spyOn(scope, '$dismiss');
                    expect(angular.isFunction(scope.cancel)).toBeTruthy();
                    scope.cancel();
                    expect(scope.$dismiss).toHaveBeenCalledWith('canceled');
                });

                it("should handle the cancel request, when not confirmed", function() {
                    spyOn(windowObj, 'confirm').andReturn(false);
                    spyOn(scope, '$dismiss');
                    expect(angular.isFunction(scope.cancel)).toBeTruthy();
                    scope.cancel();
                    expect(scope.$dismiss).not.toHaveBeenCalled();
                });

                it("should handle the processCompleted request", function() {
                    spyOn(scope, '$close');
                    spyOn(installApplicationService, 'publishApplication').andReturn(successPromise);
                    expect(angular.isFunction(scope.processCompleted)).toBeTruthy();
                    scope.processCompleted();
                    expect(scope.$close).toHaveBeenCalledWith(true);
                });
            });

            describe("WizardFileSelectionController", function() {
                var ctrl, uploadHandler;
                beforeEach(inject(function($rootScope, $httpBackend, $controller) {
                    initGlobalVariables($rootScope, $httpBackend, $controller);
                    uploadHandler = { upload: function() {}};
                    ctrl = loadController('WizardFileSelectionController', {
                        'vertxEventBusService': vertxEventBusService,
                        '$upload': uploadHandler
                    });
                    scope.progressIndicator = {};
                    scope.descriptor = {};
                }));

                it("should be defined", function() {
                    expect(ctrl).not.toBeUndefined();
                });

                it("should handle the onFileSelect event", function() {
                    spyOn(uploadHandler, 'upload').andReturn(successPromise);
                    var matchingFile = {name:'pickMePickMe.war'};
                    scope.onFileSelect([{name:'notMe.ear'}, matchingFile]);
                    expect(scope.descriptor.fileName).toEqual(matchingFile.name);
                });

                it("should handle the StartFileExtraction event", function() {
                    eventHandler['StartFileExtraction']({});
                    expect(scope.fileExtractionInProgress).toBeTruthy();
                });

                it("should handle the FileExtractionProgress event", function() {
                    scope.fileExtractionInProgress = true;
                    eventHandler['FileExtractionProgress']('{"taskCompleted":false, "message":"test"}');
                    expect(scope.fileExtractionInProgress).toBeTruthy();
                    expect(scope.progressIndicator.status).not.toBeUndefined();
                    expect(scope.progressIndicator.status).toEqual("test");
                });

                it("should handle the FileExtractionProgress - completed event", function() {
                    eventHandler['FileExtractionProgress']('{"taskCompleted":true}');
                    expect(scope.fileExtractionInProgress).not.toBeTruthy();
                });


            });

            describe("WizardLibFilterController", function() {
                var ctrl;
                beforeEach(inject(function($rootScope, $httpBackend, $controller) {
                    initGlobalVariables($rootScope, $httpBackend, $controller);
                    scope.progressIndicator = {};
                    scope.descriptor = {};
                    ctrl = loadController('WizardLibFilterController', {
                        'vertxEventBusService': vertxEventBusService
                    });
                }));

                it("should be defined", function() {
                    expect(ctrl).not.toBeUndefined();
                });

                it("should handle the resource selection", function() {
                    var resource = {id:1};
                    scope.descriptor.selectedResources = [];
                    scope.addRemoveResource(resource, {target:{checked:true}});
                    expect(scope.descriptor.selectedResources).not.toBeUndefined();
                    expect(scope.descriptor.selectedResources.length).toEqual(1);
                    expect(scope.descriptor.selectedResources[0]).toEqual(resource);
                });

                it("should handle the resource deselection", function() {
                    var resource = {id:1};
                    scope.descriptor.selectedResources = [resource, {id:2}];
                    scope.addRemoveResource(resource, {target:{checked:false}});
                    expect(scope.descriptor.selectedResources).not.toBeUndefined();
                    expect(scope.descriptor.selectedResources.length).toEqual(1);
                });

            });

            describe("WizardFieldEditorController", function() {
                var ctrl, fieldService;
                beforeEach(inject(function($rootScope, $httpBackend, $controller, FieldService) {
                    initGlobalVariables($rootScope, $httpBackend, $controller);
                    scope.progressIndicator = {};
                    scope.descriptor = {};
                    fieldService = FieldService;
                    ctrl = loadController('WizardFieldEditorController', {
                        'vertxEventBusService': vertxEventBusService,
                        'FieldService': fieldService
                    });
                }));

                it("should be defined", function() {
                    expect(ctrl).not.toBeUndefined();
                });

                it("should handle the validateField request", function() {
                    var sessionID = 'aa123';
                    scope.descriptor.sessionID=sessionID;
                    var field = {id:1};
                    spyOn(fieldService, 'validateField');
                    scope.validateField(field, "aaa");
                    expect(fieldService.validateField).toHaveBeenCalledWith(field, "aaa", sessionID);
                });

                it("should handle the activateField request", function() {
                    var e = {
                        className:'b',
                        fieldName:'a',
                        contextPath:'c'
                    };
                    var field = {
                        id:1,
                        element: e,
                        active: false
                    };
                    var field2 = {
                        id:2,
                        element: {
                            className:'b',
                            fieldName:'m',
                            contextPath:'c'
                        },
                        active:false
                    };
                    var root = {
                        id:0,
                        className:'b',
                        fieldName:'b',
                        contextPath:'c',
                        children: [field, field2]
                    };
                    scope.descriptor.fieldContainer=root;
                    scope.activateField(e);
                    expect(field.active).toBeTruthy();
                    expect(root.active).toBeUndefined();
                    expect(field2.active).not.toBeTruthy();
                });

            });

            describe("WizardSummaryController", function() {
                var ctrl, inactive, active, root;
                beforeEach(inject(function($rootScope, $httpBackend, $controller) {
                    initGlobalVariables($rootScope, $httpBackend, $controller);
                    scope.progressIndicator = {};
                    scope.descriptor = {};
                    inactive = {
                        id:1,
                        active: false
                    };
                    active = {
                        id:2,
                        active:true
                    };
                    root = {
                        id:0,
                        children: [inactive, active]
                    };
                    scope.descriptor.fieldContainer = root;
                    ctrl = loadController('WizardSummaryController', {
                        'vertxEventBusService': vertxEventBusService
                    });

                }));

                it("should be defined", function() {
                    expect(ctrl).not.toBeUndefined();
                });

                it("should show only activated fields", function() {
                    expect(scope.descriptor.editedFields).not.toBeUndefined();
                    expect(scope.descriptor.editedFields.length).toEqual(1);
                    expect(scope.descriptor.editedFields[0]).toEqual(active);
                });

            });

            describe("StepEvaluator", function() {
                var stepEvaluator;
                beforeEach(inject(function($rootScope, $httpBackend, $controller, StepEvaluator) {
                    initGlobalVariables($rootScope, $httpBackend, $controller);
                    stepEvaluator = StepEvaluator;

                }));

                it("should be defined", function() {
                    expect(stepEvaluator).not.toBeUndefined();
                });

                it("should expose the commit function for each step", function() {
                    expect(angular.isFunction(stepEvaluator.commitSelectFile)).toBeTruthy();
                    expect(angular.isFunction(stepEvaluator.commitFilterClasses)).toBeTruthy();
                    expect(angular.isFunction(stepEvaluator.commitEditProperties)).toBeTruthy();
                });

            });

            describe("wizardStepTitle", function() {
                var element;
                beforeEach(inject(function($rootScope, $httpBackend, $controller, $compile) {
                    initGlobalVariables($rootScope, $httpBackend, $controller);
                    spyOn(scope, '$emit');
                    element = angular.element('<div data-wizard-step-title="{{title}}"></div>');
                    scope.title='test';
                    $compile(element)(scope);
                    $httpBackend.expectGET('/translations?lang=en').respond(200, {}, {}, "OK");
                }));

                it("should trigger the title update", function() {
                    scope.$digest();
                    expect(element.attr('data-wizard-step-title')).toEqual('test');
                    expect(scope.$emit).toHaveBeenCalledWith('stepTitleChanged', 'test');
                });

            });
        });

    });


})();