define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:HomeCtrl
   * @description
   * # HomeCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.HomeCtrl', [])
    .controller('HomeCtrl', function (UserSvc) {
    	var vm = this;
    	vm.user = {};
    	UserSvc.getUser().then(function(user) {
    		vm.user = user;
    	});
    });
});
