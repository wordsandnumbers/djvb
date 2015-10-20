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
        .controller('LoginCtrl', function ($log, $http, $httpParamSerializer, $location, $ionicPopup, UserSvc) {
            var vm = this;
            vm.digitsLogin = digitsLogin;
            vm.emailLogin = emailLogin;

            function digitsLogin(event) {
                Digits.logIn()
                .done(onLogin) /*handle the response*/
                .fail(onLoginFailure);
            }

            function onLogin(loginResponse){
                // Send headers to your server and validate user by calling Digits’ API
                var oAuthHeaders = loginResponse.oauth_echo_headers;
                var verifyData = {
                    'authHeader': oAuthHeaders['X-Verify-Credentials-Authorization'],
                    'apiUrl': oAuthHeaders['X-Auth-Service-Provider']
                };

				$http({
					method : 'POST',
					url : '/login/login',
					headers : {
						'Content-Type' : 'application/x-www-form-urlencoded'
					},
					data : $httpParamSerializer(verifyData)
				}).then(function(response) {
					checkUser();
				}, function(response) {
					$ionicPopup.alert({
						title: "Error",
						template: JSON.stringify(response.data)
					});
				});
			}

            function onLoginFailure() {
				$ionicPopup.alert({
					title: "Error",
					template: "Couldn't do Digits login."
				});
            }
            
            function emailLogin() {
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
					$ionicPopup.alert({
						title: "Error",
						template: JSON.stringify(response.data)
					});
				});
            }
            
            function checkUser() {
	    		UserSvc.getUser().then(function(user) {
	    			if (_.isEmpty(user.screenName) || _.isEmpty(user.email)) {
		    			UserSvc.showSettingsModal();
	    			}
	    			$location.url('/home');
	    		}, function(response) {
					$ionicPopup.alert({
						title: "Error",
						template: JSON.stringify(response.data)
					});
	    		});
            }
            
            /*var authenticate = function(credentials, callback) {

                var headers = credentials ? {authorization : "Basic "
                    + btoa(credentials.username + ":" + credentials.password)
                } : {};

                $http.get('user', {headers : headers}).success(function(data) {
                  if (data.name) {
                    $rootScope.authenticated = true;
                  } else {
                    $rootScope.authenticated = false;
                  }
                  callback && callback();
                }).error(function() {
                  $rootScope.authenticated = false;
                  callback && callback();
                });

              }

              authenticate();
              $scope.credentials = {};
              $scope.login = function() {
                  authenticate($scope.credentials, function() {
                    if ($rootScope.authenticated) {
                      $location.path("/");
                      $scope.error = false;
                    } else {
                      $location.path("/login");
                      $scope.error = true;
                    }
                  });
              };*/
            
            
        });
});