/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Controller: PlaylistCtrl', function () {

    // load the controller's module
    beforeEach(module('djvbApp.controllers.PlaylistCtrl'));

    var PlaylistCtrl;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
      PlaylistCtrl = $controller('PlaylistCtrl', {
        // place here mocked dependencies
      });
    }));

    it('should attach a list of awesomeThings to the scope', function () {
      expect(PlaylistCtrl.awesomeThings.length).toBe(3);
    });
  });
});
