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
	.service('UserSvc', function ($rootScope, $http, $q, $ionicModal) {
		
		var user,
			modalScope = $rootScope.$new(), 
			userSettingsModal;
		
		$ionicModal.fromTemplateUrl('views/usersettingsmodal.html', {
			scope: modalScope,
			animation: 'slide-in-up'
		}).then(function(modal) {
            modalScope.modal = modal;
			userSettingsModal = modal;
			modalScope.userForm = {};

            modalScope.closeModal = function () {
                modalScope.modal.hide();
            }
            
            modalScope.putUser = function () {
            	putUser(modalScope.user).then(function () {
            		modalScope.modal.hide();
            	});
            }
		});
		
		return {
			getUser: getUser, 
			putUser: putUser, 
			showSettingsModal: showSettingsModal
		};
		
		function getUser() {
			return $q(function (resolve, reject) {
				if (user === undefined) {
					$http.get('/api/v1/user/user').then(function(config) {
						user = config.data;
						resolve(user);
					}, function(error) {
						reject(error);
					});
				} else {
					resolve(user);
				}
			});
		}
		
		function putUser(user) {
			return $q(function (resolve, reject) {
				$http.put('/api/v1/user', user).then(function(config) {
					user = config.data;
					resolve(user);
				}, function(error) {
					reject(error);
				});
			});
		}

		function showSettingsModal() {
			getUser().then(function(user) {
				userSettingsModal.show().then(function() {
		            modalScope.user = user;
				});
			})
		}
	
	});
});
