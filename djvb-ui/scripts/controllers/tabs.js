define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:TabsCtrl
   * @description
   * # TabsCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.TabsCtrl', [])
    .controller('TabsCtrl', function ($state) {
    	var vm = this;
    	vm.goToState = goToState;
    	function goToState(state) {
    		$state.go(state, {});
    	}
    });
});
