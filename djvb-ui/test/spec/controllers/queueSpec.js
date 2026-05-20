/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Controller: QueueCtrl', function () {

    // load the controller's module
    beforeEach(module('djvbApp.controllers.QueueCtrl'));

    var QueueCtrl;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
      QueueCtrl = $controller('QueueCtrl', {
        // place here mocked dependencies
      });
    }));

    it('should attach a list of awesomeThings to the scope', function () {
      expect(QueueCtrl.awesomeThings.length).toBe(3);
    });
  });
});
