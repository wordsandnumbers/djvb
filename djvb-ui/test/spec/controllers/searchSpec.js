/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Controller: SearchCtrl', function () {

    // load the controller's module
    beforeEach(module('djvbApp.controllers.SearchCtrl'));

    var SearchCtrl;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
/*      SearchCtrl = $controller('SearchCtrl', {
        // place here mocked dependencies
      });*/
    }));

    xit('should attach a list of awesomeThings to the scope', function () {
      expect(SearchCtrl.awesomeThings.length).toBe(3);
    });
  });
});
