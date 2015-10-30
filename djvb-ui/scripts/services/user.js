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
	.service('UserSvc', function ($rootScope, $http, $q, $ionicModal, $location) {
		
		var user,
			modalScope = $rootScope.$new();

		return {
			getUser: getUser, 
			putUser: putUser, 
			showSettingsModal: showSettingsModal, 
			logout: logout
		};
		
		function getUser() {
			return $q(function (resolve, reject) {
				if (user === undefined) {
					$http.get('/api/v1/user/user').then(function(config) {
						user = config.data;
						$rootScope.authenticated = true;
						resolve(user);
					}, function(error) {
						reject(error);
					});
				} else {
					resolve(user);
				}
			});
		}
		
		function putUser(updatedUser) {
			return $q(function (resolve, reject) {
				$http.put('/api/v1/user', updatedUser).then(function(config) {
					user = config.data;
					resolve(user);
				}, function(error) {
					reject(error);
				});
			});
		}

		function logout() {
			return $q(function (resolve, reject) {
				$http.get('/logout').then(function(response) {
					$rootScope.authenticated = false;
					resolve(response);
				}, function(response) {
					reject(response);
				})			
			})
		}
		
		function showSettingsModal() {
			getUser().then(function(user) {
	            modalScope.user = user;
	            modalScope.userCopy = angular.copy(user);
	            createModal();
	        })
		}
		
		function createModal() {
			$ionicModal.fromTemplateUrl('views/usersettingsmodal.html', {
				scope: modalScope,
				animation: 'slide-in-up'
			}).then(function(modal) {
	            modalScope.modal = modal;
				modal.show();

	            modalScope.closeModal = function () {
	                modalScope.modal.hide();
	    			modalScope.modal.remove();
	            }
	            
	            modalScope.logout = function() {
	            	logout().then(function() {
	            		modalScope.closeModal();
                        $location.path('/login');
	            	});
	            }
	            
	            modalScope.putUser = function () {
            		modalScope.closeModal();
	            	putUser(modalScope.userCopy);
	            }
			});			
		}
	
	});
});
