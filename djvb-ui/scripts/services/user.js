define(['angular', 'lodash'], function (angular, _) {
  'use strict';

  /**
   * @ngdoc service
   * @name djvbApp.User
   * @description
   * # User
   * Service in the djvbApp.
   */
  angular.module('djvbApp.services.UserSvc', [])
	.service('UserSvc', function ($rootScope, $http, $q, $ionicModal, $location, constants) {
		
		var user,
			userPromise,
			modalScope = $rootScope.$new();

		return {
			getUser: getUser, 
			putUser: putUser, 
			showSettingsModal: showSettingsModal, 
			logout: logout
		};
		
		function getUser() {
			if (userPromise === undefined || _.get(userPromise, '$$state.status') === 2) {
				userPromise = $q(function (resolve, reject) {
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
			return userPromise;
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
					user = undefined;
					userPromise = undefined;
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
			$ionicModal.fromTemplateUrl(constants.resourcesBaseUrl + '/views/usersettingsmodal.html', {
				scope: modalScope,
				animation: 'slide-in-up',
				backdropClickToClose: false,
				hardwareBackButtonClose: false
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
