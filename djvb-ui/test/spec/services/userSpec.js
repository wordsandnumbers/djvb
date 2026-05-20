/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Service: User', function () {

    // load the service's module
    beforeEach(module('djvbApp.services.User'));

    // instantiate service
    var User;
    beforeEach(inject(function (_User_) {
      User = _User_;
    }));

    it('should do something', function () {
      expect(!!User).toBe(true);
    });

  });
});
