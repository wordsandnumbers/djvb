define(['angular', 'firebase', 'firebaseui'], function (angular, firebase, firebaseui) {
    'use strict';

    /**
     * @ngdoc function
     * @name djvbApp.controller:LoginCtrl
     * @description
     * # LoginCtrl
     * Controller of the djvbApp
     */
    angular.module('djvbApp.controllers.LoginCtrl', [])
	.directive('overwriteEmail', function() {
		var EMAIL_REGEXP = /.+@.+\..+/i; // Email needs an `@` and a `.`

		return {
			require : 'ngModel',
			restrict : '',
			link : function(scope, elm, attrs, ctrl) {
				// only apply the validator if ngModel
				// is present and Angular has added
				// the email validator
				if (ctrl && ctrl.$validators.email) {

					// this will overwrite the default
					// Angular email validator
					ctrl.$validators.email = function(
							modelValue) {
						return ctrl
								.$isEmpty(modelValue)
								|| EMAIL_REGEXP
										.test(modelValue);
					};
				}
			}
		};
	})
	.controller('LoginCtrl', function (
		$rootScope, 
		$log, 
		$http, 
		$httpParamSerializer, 
		$location, 
		$ionicPopup, 
		$ionicLoading, 
		$state,
		UserSvc
	) {
        var vm = this;
        vm.emailLogin = emailLogin;

        var uiConfig = {
            callbacks: {
                signInSuccess: function(currentUser, credential, redirectUrl) {
                    $ionicLoading.show();

                    firebase.auth().currentUser.getIdToken(/* forceRefresh */ true).then(function(idToken) {
                        // Send token to your backend via HTTPS
                        $http({
                            method : 'POST',
                            url : '/login/login',
                            headers : {
                                'Content-Type' : 'application/x-www-form-urlencoded'
                            },
                            data : $httpParamSerializer({'idToken':idToken})
                        }).then(function() {
                            $rootScope.authenticated = true;
                            $location.url('/home');
                        }, function(response) {
                            $rootScope.authenticated = false;
                            $ionicPopup.alert({
                                title: "Error",
                                template: JSON.stringify(response.data)
                            });
                            $location.url('/login');
                        }).finally(function () {
                            $ionicLoading.hide();
                        });
                    });
                }
            },
			signInOptions: [
				firebase.auth.EmailAuthProvider.PROVIDER_ID,
				{
					provider: firebase.auth.PhoneAuthProvider.PROVIDER_ID,
					recaptchaParameters: {
						type: 'image', // 'audio'
						size: 'normal', // 'invisible' or 'compact'
						badge: 'bottomleft' //' bottomright' or 'inline' applies to invisible.
					}
				}
			],
			// Terms of service url.
			tosUrl: ''
		};

		// Initialize the FirebaseUI Widget using Firebase.
		var ui = new window.firebaseui.auth.AuthUI(firebase.auth());

		// The start method will wait until the DOM is loaded.
		ui.start('#firebaseui-auth-container', uiConfig);
                
        function emailLogin() {
        	$ionicLoading.show();

			$http({
				method : 'POST',
				url : '/login/login',
				headers : {
					'Content-Type' : 'application/x-www-form-urlencoded'
				},
				data : $httpParamSerializer({'name':vm.email})
			}).then(function(response) {
				checkUser();
			}, function(response) {
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify(response.data)
				});
			}).finally(function () {
				$ionicLoading.hide();
			});
        }
        
        function checkUser() {
    		UserSvc.getUser().then(function() {
    			$state.go('tabs.home');
    		}, function(response) {
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify(response.data)
				});
    		});
        }
	});
});