/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Controller: PlaylistsCtrl', function () {

    // load the controller's module
    beforeEach(module('djvbApp.controllers.PlaylistsCtrl'));

    var PlaylistsCtrl;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
      PlaylistsCtrl = $controller('PlaylistsCtrl', {
        // place here mocked dependencies
      });
    }));

    it('should attach a list of awesomeThings to the scope', function () {
      expect(PlaylistsCtrl.awesomeThings.length).toBe(3);
    });
  });
});
