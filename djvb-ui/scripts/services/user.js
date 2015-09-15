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
		
		var user = {};
		
		return {
			getUser: getUser, 
			putUser: putUser
		};
		
		function getUser() {
			return $q(function (resolve, reject) {
				$http.get('/user').then(function(config) {
					user = config.data;
					resolve(user);
				}, function(error) {
					reject(error);
				});
			});
		}
		
		function putUser(user) {
			return $q(function (resolve, reject) {
				$http.put('/user', user).then(function(config) {
					user = config.data;
					resolve(user);
				}, function(error) {
					reject(error);
				});
			});
		}
	});
});
