/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Controller: HomeCtrl', function () {

    // load the controller's module
    beforeEach(module('djvbApp.controllers.HomeCtrl'));

    var HomeCtrl;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
      HomeCtrl = $controller('HomeCtrl', {
        // place here mocked dependencies
      });
    }));

    it('should attach a list of awesomeThings to the scope', function () {
      expect(HomeCtrl.awesomeThings.length).toBe(3);
    });
  });
});
