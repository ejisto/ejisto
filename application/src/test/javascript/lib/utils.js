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

//these variables have been INTENTIONALLY published to the global scope

var scope, httpBackend, controller, rootScope;
var loadController = function(name, mocks) {
    var baseMocks = {'$scope': scope};
    var injectedMocks = angular.extend({}, baseMocks, mocks);
    return controller(name, injectedMocks);
};
var initGlobalVariables = function($rootScope, $httpBackend, $controller) {
    scope = $rootScope.$new();
    httpBackend = $httpBackend;
    controller = $controller;
    rootScope = $rootScope;
};

var eventHandler = [];
var vertxEventBusService = {
    on: function(name, fn) {
        eventHandler[name] = fn;
    },
    send: jasmine.createSpy('send')
};
var successPromise = {
    then: function(s, e) {
        s({});
        return this;
    },
    success: function(s, e) {
        return this.then(s,e);
    },
    error: function() {
        return {};
    }
};
var errorPromise = function(status) {
    return {
        then: function(s, e) {
            e({status: status});
        },
        error: function() {
            return {};
        }
    };
};

