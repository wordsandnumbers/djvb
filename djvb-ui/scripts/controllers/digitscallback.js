define(['angular', 'digits'], function (angular, Digits) {
    'use strict';

    /**
     * @ngdoc function
     * @name djvbApp.controller:DigitsCallbackCtrl
     * @description
     * # DigitsCallbackCtrl
     * Controller of the djvbApp. Only handles auth with djvb and redirection.
     */
    angular.module('djvbApp.controllers.DigitsCallbackCtrl', [])
	.controller('DigitsCallbackCtrl', function ($rootScope, $log, $http, $httpParamSerializer, $location, $ionicPopup, $ionicLoading, UserSvc, $stateParams) {
            var vm = this;
        
            if ($stateParams['X-Verify-Credentials-Authorization'] !== undefined 
            	&& $stateParams['X-Auth-Service-Provider'] !== undefined) {
            	onLogin();
            } else {
				$ionicPopup.alert({
					title: "Error",
					template: "There was a problem with the Digits callback."
				});
    			$location.url('/login');
            }
            
            function onLogin() {
            	$ionicLoading.show();
            	
				var verifyData = {
					authHeader: $stateParams['X-Verify-Credentials-Authorization'],
					apiUrl: $stateParams['X-Auth-Service-Provider']
				};
                    
				$http({
					method : 'POST',
					url : '/login/login',
					headers : {
						'Content-Type' : 'application/x-www-form-urlencoded'
					},
					data : $httpParamSerializer(verifyData)
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
			}
		});
});