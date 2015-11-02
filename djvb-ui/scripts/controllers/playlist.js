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
    .controller('PlaylistCtrl', function ($scope, $stateParams, $ionicActionSheet, $ionicPopup, $location, PlaylistsSvc, ActionSheetSvc) {
    	var vm = this;
    	vm.selectSong = selectSong;
    	vm.playlistMenu = playlistMenu;
    	vm.renamePlaylistPopup = renamePlaylistPopup;
		
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
    	
    	function renamePlaylistPopup() {
    		vm.playlistCopy = angular.copy($scope.vm.playlist);
	        var popup = $ionicPopup.show({
				template : '<label class="item item-input">' +
		        	'<input ng-model="vm.playlistCopy.name" type="text" placeholder="Playlist Name">' +
		        	'</label>',
				title : 'Rename Playlist',
				scope : $scope,
				buttons : [ 
					{
						text : 'Cancel'
					}, {
						text : '<b>OK</b>',
						type : 'button-positive',
						onTap : function(e) {
							if (!$scope.vm.playlistCopy.name) {
								e.preventDefault();
							} else {
					        	PlaylistsSvc.updatePlaylist($scope.vm.playlistCopy).then(function() {
					        		$scope.vm.playlistName = '';
					        		return $scope.vm.playlistName;
					        	}, function() {
									$ionicPopup.alert({
										title: 'Error',
										template: 'Couldn\'t change name.'
									});
					        	});
							}
						}
					} 
				]
			});
	        return popup;
        }
    	
    	function playlistMenu() {
			$ionicActionSheet.show({
				titleText : vm.playlist.title, 
				buttons: [
					{text: 'Rename'}/*, 
					{text: 'Reorder'}*/
				], 
				buttonClicked : function(index) {
					switch (index) {
						case 0:
							vm.renamePlaylistPopup();
							break;
					}
					return true;
				}, 
				destructiveText: 'Delete', 
				cancelText : 'Cancel',
				destructiveButtonClicked : function() {
					PlaylistsSvc.deletePlaylist(vm.playlist).then(function(response) {
						// Success
		    			$location.url('/playlists');
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
