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
	.service('UserSvc', function ($rootScope, $http, $q, $ionicModal, $ionicPopover, $location, constants) {
		
		var user = {},
			userPromise,
			modalScope = $rootScope.$new(),
			userColors = ['#72C2FF', '#6798E6', '#CEA1E1', '#FFADED', '#FC809B', '#FFC787', '#FFF074', '#A6FC81', '#09E6AE', '#18C4C7']

		return {
			getUser: getUser, 
			putUser: putUser, 
			showSettingsModal: showSettingsModal, 
			logout: logout
		};
		
		function getUser() {
			if (userPromise === undefined || _.get(userPromise, '$$state.status') === 2) {
				userPromise = $q(function (resolve, reject) {
					$http.get('/api/v1/user/user').then(function(response) {
						angular.copy(response.data, user);
						checkUser();
						$rootScope.authenticated = true;
						resolve(user);
					}, function(errorResponse) {
						reject(errorResponse);
					});
				});
			}
			return userPromise;
		}
		
		function putUser(updatedUser) {
			return $q(function (resolve, reject) {
				$http.put('/api/v1/user', updatedUser).then(function(response) {
					angular.copy(response.data, user);
					resolve(user);
				}, function(response) {
					reject(response);
				});
			});
		}

		function logout() {
			return $q(function (resolve, reject) {
				$http.get('/logout').then(function(response) {
					$rootScope.authenticated = false;
					user = {};
					userPromise = undefined;
					resolve(response);
				}, function(response) {
					reject(response);
				})			
			})
		}

		function checkUser() {
			if (_.isEmpty(user.screenName) || _.isEmpty(user.email)) {
				showSettingsModal();
			}
		}

		function showSettingsModal() {
			getUser().then(function(response) {
	            modalScope.user = response;
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
				
				$ionicPopover.fromTemplateUrl(constants.resourcesBaseUrl + '/views/colorpickerpopover.html', {
					scope: modalScope
				}).then(function(popover) {
					modalScope.popover = popover;
				});

				modalScope.userColors = userColors;

				modalScope.setColor = function (color) {
					modalScope.userCopy.color = color;
					modalScope.popover.hide();
				};
				
				modalScope.pickColor = function ($event) {
					modalScope.popover.show($event);
				};

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
