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
    .controller('HomeCtrl', function (UserSvc, PlaylistsSvc, TagsSvc) {
    	var vm = this;
    	vm.user;
    	vm.playlists;
    	vm.favorites;
    	vm.playHistory;
    	vm.categories;
    	
    	UserSvc.getUser().then(function(user) {
    		vm.user = user;
    	});
    	PlaylistsSvc.getPlaylistsList().then(function(playlists) {
    		vm.playlists = playlists;
    	});
    	PlaylistsSvc.getPlayHistoryList().then(function(playHistory) {
    		vm.playHistory = playHistory;
    	});
    	PlaylistsSvc.getFavoritesList().then(function(favorites) {
    		vm.favorites = favorites;
    	});
		TagsSvc.getTags().then(function(response) {
			vm.categories = response.categories;
		});
    	
    	vm.showUserSettingsModal = showUserSettingsModal;
    	
    	function showUserSettingsModal() {
    		UserSvc.showSettingsModal();
    	}
    });
});
