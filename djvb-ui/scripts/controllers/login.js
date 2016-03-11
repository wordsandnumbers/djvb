define(['angular', 'digits'], function (angular, Digits) {
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
	.controller('LoginCtrl', function ($rootScope, $log, $http, $httpParamSerializer, $location, $ionicPopup, $ionicLoading, UserSvc) {
        var vm = this;
        vm.digitsLogin = digitsLogin;
        vm.emailLogin = emailLogin;
    
        function digitsLogin(event) {
            Digits.logIn({
            	callbackURL: $location.protocol() + '://' + $location.host() + ':'
            	+ $location.port() + '/resources/index.html#digitscallback'
            });
        }
                
        function emailLogin() {
        	$ionicLoading.show();

			$http({
				method : 'POST',
				url : '/login/login',
				headers : {
					'Content-Type' : 'application/x-www-form-urlencoded'
				},
				data : $httpParamSerializer({'apiUrl':vm.email})
			}).then(function(response) {
				checkUser();
			}, function(response) {
                $ionicLoading.hide();
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify(response.data)
				});
			});
        }
        
        function checkUser() {
    		UserSvc.getUser().then(function(user) {
                $ionicLoading.hide();
    			if (_.isEmpty(user.screenName) || _.isEmpty(user.email)) {
	    			UserSvc.showSettingsModal();
    			}
    			$location.url('/home');
    		}, function(response) {
                $ionicLoading.hide();
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify(response.data)
				});
    		});
        }
	});
});