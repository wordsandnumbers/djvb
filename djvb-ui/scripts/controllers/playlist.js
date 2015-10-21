define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:PlaylistCtrl
   * @description
   * # PlaylistCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.PlaylistCtrl', [])
    .controller('PlaylistCtrl', function ($stateParams, $ionicActionSheet, $ionicPopup, $location, PlaylistsSvc, ActionSheetSvc) {
    	var vm = this;
    	vm.selectSong = selectSong;
    	
    	PlaylistsSvc.getPlaylistsList().then(function (response) {
    		vm.playlists = response;
    		var foundPlaylist = _.find(vm.playlists, {id: $stateParams.playlistId});
    		if (foundPlaylist !== undefined) {
    			vm.playlist = foundPlaylist;
    		} else {
    			$location.url('/playlists');
    		}
    	})
    	
        function selectSong(song) {
    		ActionSheetSvc.playlistSongActions(vm.playlist, song);
        }		
    });
});
