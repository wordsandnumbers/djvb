/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Service: Queue', function () {

    // load the service's module
    beforeEach(module('djvbApp.services.Queue'));

    // instantiate service
    var Queue;
    beforeEach(inject(function (_Queue_) {
      Queue = _Queue_;
    }));

    it('should do something', function () {
      expect(!!Queue).toBe(true);
    });

  });
});
