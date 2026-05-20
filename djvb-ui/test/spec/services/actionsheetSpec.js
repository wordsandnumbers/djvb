/*jshint unused: vars */
define(['angular', 'angular-mocks', 'app'], function(angular, mocks, app) {
  'use strict';

  describe('Service: ActionSheet', function () {

    // load the service's module
    beforeEach(module('djvbApp.services.ActionSheet'));

    // instantiate service
    var ActionSheet;
    beforeEach(inject(function (_ActionSheet_) {
      ActionSheet = _ActionSheet_;
    }));

    it('should do something', function () {
      expect(!!ActionSheet).toBe(true);
    });

  });
});
