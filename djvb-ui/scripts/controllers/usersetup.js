define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:UserSetupCtrl
   * @description
   * # UserSetupCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.UserSetupCtrl', [])
    .controller('UserSetupCtrl', function (user, UserSvc) {
    	var vm = this;
    	vm.user = user;
    	
    	vm.putUser = putUser;
    	
    	function putUser() {
    		UserSvc.putUser(vm.user).then(function(user) {
    			console.log('updated user');
    			vm.userForm.$setPristine();
    		});
    	}
    	
    });
});
