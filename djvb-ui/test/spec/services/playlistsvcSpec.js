/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Service: PlaylistSvc', function () {

    // load the service's module
    beforeEach(module('djvbUiApp.services.PlaylistSvc'));

    // instantiate service
    var PlaylistSvc;
    beforeEach(inject(function (_PlaylistSvc_) {
      PlaylistSvc = _PlaylistSvc_;
    }));

    it('should do something', function () {
      expect(!!PlaylistSvc).toBe(true);
    });

  });
});
