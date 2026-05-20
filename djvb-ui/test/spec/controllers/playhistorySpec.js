/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Controller: PlayHistoryCtrl', function () {

    // load the controller's module
    beforeEach(module('djvbApp.controllers.PlayHistoryCtrl'));

    var PlayHistoryCtrl;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
      PlayHistoryCtrl = $controller('PlayHistoryCtrl', {
        // place here mocked dependencies
      });
    }));

    it('should attach a list of awesomeThings to the scope', function () {
      expect(PlayHistoryCtrl.awesomeThings.length).toBe(3);
    });
  });
});
