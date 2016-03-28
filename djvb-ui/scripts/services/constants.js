define(['angular'], function (angular) {
	'use strict';

	  /**
	   * @ngdoc service
	   * @name djvbApp.User
	   * @description
	   * # User
	   * Service in the djvbApp.
	   */
	angular.module('djvbApp.services.Constants', [])
		.constant('constants', {
			resourcesBaseUrl: '/resources'
		});
});
