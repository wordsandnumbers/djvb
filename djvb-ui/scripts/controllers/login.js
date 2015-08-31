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
        .controller('LoginCtrl', function ($log, $http) {
            var vm = this;
            vm.digitsLogin = digitsLogin;


            function digitsLogin(event) {
                Digits.logIn()
                .done(onLogin) /*handle the response*/
                .fail(onLoginFailure);
            }

            function onLogin(loginResponse){
                // Send headers to your server and validate user by calling Digits’ API
                var oAuthHeaders = loginResponse.oauth_echo_headers;
                var verifyData = {
                    authHeader: oAuthHeaders['X-Verify-Credentials-Authorization'],
                    apiUrl: oAuthHeaders['X-Auth-Service-Provider']
                };

                $http.get('http://localhost:8080/login/verify', {params: verifyData})
                    .then(function(){ 
                        
                    });
            }

            function onLoginFailure() {
                $log.error("Couldn't do Digits login.");
            }
        });
});