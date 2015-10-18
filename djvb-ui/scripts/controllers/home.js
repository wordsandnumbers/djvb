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
    .controller('HomeCtrl', function (UserSvc, PlaylistsSvc) {
    	var vm = this;
    	vm.user = {};
    	vm.playlists;
    	UserSvc.getUser().then(function(user) {
    		vm.user = user;
    	});
    	PlaylistsSvc.getPlaylistsList().then(function(playlists) {
    		vm.playlists = playlists;
    	});
    	
    	vm.showUserSettingsModal = showUserSettingsModal;
    	
    	function showUserSettingsModal() {
    		UserSvc.showSettingsModal();
    	}
    });
});
