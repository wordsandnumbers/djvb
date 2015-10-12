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
    .controller('PlaylistCtrl', function ($stateParams, $ionicActionSheet, $ionicPopup, PlaylistsSvc) {
    	var vm = this;
    	vm.selectSong = selectSong;
    	
    	PlaylistsSvc.getPlaylistsList().then(function (response) {
    		vm.playlists = response;
    		vm.playlist = _.find(vm.playlists, {id: $stateParams.playlistId});
    	})
    	
        function selectSong(songIndex) {
    		var song = vm.playlist.songs[songIndex];
			var hideSheet = $ionicActionSheet.show({
				titleText : song.title + ' - ' + song.artist,
				destructiveText: 'Delete', 
				cancelText : 'Cancel',
				destructiveButtonClicked : function() {
					PlaylistsSvc.deleteSongFromPlaylist(vm.playlist, songIndex).then(function(response) {
						// Success
					}, function(config) {
						// Error
						$ionicPopup.alert({
							title: "Error",
							template: JSON.stringify(config.data)
						});
					});
					return true;
				}
			});
        }		
    });
});
