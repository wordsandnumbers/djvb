/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Service: Playlist', function () {

    // load the service's module
    beforeEach(module('djvbApp.services.Playlist'));

    // instantiate service
    var Playlist;
    beforeEach(inject(function (_Playlist_) {
      Playlist = _Playlist_;
    }));

    it('should do something', function () {
      expect(!!Playlist).toBe(true);
    });

  });
});
