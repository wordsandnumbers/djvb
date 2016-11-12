define(['angular', 'lodash'], function (angular, _) {
	'use strict';

	/**
	 * @ngdoc service
	 * @name djvbApp.User
	 * @description
	 * # User
	 * Service in the djvbApp.
	 */
	angular.module('djvbApp.services.UserSvc', ['ngImgCrop', 'ngFileUpload'])
		.directive('fileInput', function () {
			return {
				restrict: 'A',
				link: function (scope, element, attrs) {
					var onChangeHandler = scope.$eval(attrs.fileInput);
					element.bind('change', onChangeHandler);
				}
			};
		})
		.service('UserSvc', function ($rootScope, $http, $q, $ionicModal, $ionicPopover, $ionicPopup, $location, constants, Upload) {

			var user = {},
				userPromise;

			return {
				getUser: getUser,
				putUser: putUser,
				showSettingsModal: showSettingsModal,
				logout: logout
			};

			function getUser() {
				if (userPromise === undefined || _.get(userPromise, '$$state.status') === 2) {
					userPromise = $q(function (resolve, reject) {
						$http.get('/api/v1/user/user').then(function (response) {
							angular.copy(response.data, user);
							checkUser();
							$rootScope.authenticated = true;
							resolve(user);
						}, function (errorResponse) {
							reject(errorResponse);
						});
					});
				}
				return userPromise;
			}

			function putUser(updatedUser) {
				userPromise = $q(function (resolve, reject) {
					$http.put('/api/v1/user', updatedUser).then(function (response) {
						angular.copy(response.data, user);
						resolve(user);
					}, function (response) {
						reject(response);
					});
				});
				return userPromise;
			}

			function logout() {
				return $q(function (resolve, reject) {
					$http.get('/logout').then(function (response) {
						$rootScope.authenticated = false;
						user = {};
						userPromise = undefined;
						resolve(response);
					}, function (response) {
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
				getUser().then(function (response) {
					createModal();
				})
			}

			function createModal() {
				var modalScope = $rootScope.$new();
				modalScope.handleFileSelect = function (event) {
					var file = event.target.files[0];
					var reader = new FileReader();

					reader.onload = readerOnLoad;

					if (file) {
						reader.readAsDataURL(file);
					}

					function readerOnLoad(evt) {
						modalScope.$apply(function () {
							editAvatar(evt.target.result);
						});
					}
				}
				$ionicModal.fromTemplateUrl(constants.resourcesBaseUrl + '/views/usersettingsmodal.html', {
					scope: modalScope,
					animation: 'slide-in-up',
					backdropClickToClose: false,
					hardwareBackButtonClose: false
				}).then(function (modal) {

					modalScope.user = user;
					modalScope.userCopy = angular.copy(user);

					modalScope.modal = modal;
					modal.show();

					modalScope.closeModal = function () {
						modalScope.modal.hide();
						//modalScope.modal.remove();
					}

					modalScope.logout = function () {
						logout().then(function () {
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

			function dataURItoBlob(dataURI) {
				// convert base64/URLEncoded data component to raw binary data held in a string
				// http://stackoverflow.com/questions/4998908/convert-data-uri-to-file-then-append-to-formdata
				var byteString;
				var ia;
				// separate out the mime component
				var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

				if (dataURI.split(',')[0].indexOf('base64') >= 0) {
					byteString = window.atob(dataURI.split(',')[1]);
				} else {
					byteString = window.unescape(dataURI.split(',')[1]);
				}

				// write the bytes of the string to a typed array
				ia = new Uint8Array(byteString.length);
				for (var i = 0; i < byteString.length; i++) {
					ia[i] = byteString.charCodeAt(i);
				}

				return new Blob([ia], {
					type: mimeString
				});
			}

			function editAvatar(imageUrl) {

				var cropScope = $rootScope.$new();

				cropScope.avatar = {
					avatarImage: imageUrl,
					croppedAvatarImage: ''
				};

				cropScope.croppedAvatarImage = '';
				
				$ionicModal.fromTemplateUrl(constants.resourcesBaseUrl + '/views/imagecropmodal.html', {
					scope: cropScope,
					animation: 'slide-in-up'
				}).then(function (modal) {

					cropScope.modal = modal;
					modal.show();

					cropScope.closeModal = function () {
						cropScope.modal.hide();
						//modalScope.modal.remove();
					}

					cropScope.postAvatar = function () {
						Upload.upload({
							file: dataURItoBlob(cropScope.avatar.croppedAvatarImage),
							fileFormDataName: 'file',
							url: '/api/v1/user/avatar'
						}).then(function (response) {
							angular.copy(response.data, user);
							cropScope.closeModal();
						}, function () {
							$ionicPopup.alert({
								title: "Error",
								template: "Couldn't upload image."
							});
						});
					}
				});

			}

		});
});
