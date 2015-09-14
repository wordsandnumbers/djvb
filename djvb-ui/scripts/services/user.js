define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc service
   * @name djvbApp.User
   * @description
   * # User
   * Service in the djvbApp.
   */
  angular.module('djvbApp.services.UserSvc', [])
	.service('UserSvc', function ($http, $q) {
		
		return {
			getUser: getUser
		};
		
		function getUser() {
			return $q(function (resolve, reject) {
				$http.get('/user').then(function(response) {
					resolve(response);
				});
			});
		}

	});
});
