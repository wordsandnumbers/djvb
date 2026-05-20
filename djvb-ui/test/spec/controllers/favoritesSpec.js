/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Controller: FavoritesCtrl', function () {

    // load the controller's module
    beforeEach(module('djvbApp.controllers.FavoritesCtrl'));

    var FavoritesCtrl;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
      FavoritesCtrl = $controller('FavoritesCtrl', {
        // place here mocked dependencies
      });
    }));

    it('should attach a list of awesomeThings to the scope', function () {
      expect(FavoritesCtrl.awesomeThings.length).toBe(3);
    });
  });
});
