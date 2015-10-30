define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc service
   * @name djvbApp.ActionSheetSvc
   * @description
   * # ActionSheetSvc
   * Service in the djvbApp.
   */
  angular.module('djvbApp.services.ActionSheetSvc', [])
	.service('ActionSheetSvc', function ($rootScope, $ionicPopup, $ionicActionSheet, UserSvc, PlaylistsSvc, QueueSvc, $timeout) {
		var user,
			modalScope = $rootScope.$new(), 
			playlists = [], 
			queues = [];
		
    	PlaylistsSvc.getPlaylistsList().then(function(response) {
        	playlists = response;
        });
    	QueueSvc.getQueues().then(function(response) {
        	queues = response;
        });
    	UserSvc.getUser().then(function(response) {
        	user = response;
        });

		return {
    		playlistSongActions: playlistSongActions, 
    		favoriteSongActions: favoriteSongActions, 
    		playHistoryActions: playHistoryActions, 
    		searchSongActions: playHistoryActions
		};
		
		function playlistSongActions(playlist, song) {
			$ionicActionSheet.show({
				titleText : song.title + ' - ' + song.artist, 
				buttons: [
					{text: '<strong>Sing Now!</strong>'}, 
					{text: 'Add to Playlist'}
				], 
				buttonClicked : function(index) {
					switch (index) {
						case 0:
							if (queues.length > 0) {
								// Add song to queue
								addSongToQueue(song);
							} else {
								// Join a room
								var callback = function() {
									addSongToQueue(song);
								};
							    roomCodePopup(callback);
							}
							break;
						case 1:
							playlistsAction(song);
							break;
					}
					return true;
				}, 
				destructiveText: 'Delete', 
				cancelText : 'Cancel',
				destructiveButtonClicked : function() {
					PlaylistsSvc.deleteSongFromPlaylist(playlist, song).then(function(response) {
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
		
		function playHistoryActions(play) {
			$ionicActionSheet.show({
				titleText : play.title + ' - ' + play.artist, 
				buttons: [
					{text: '<strong>Sing Now!</strong>'}, 
					{text: 'Add to Playlist'}
				], 
				buttonClicked : function(index) {
					switch (index) {
						case 0:
							if (queues.length > 0) {
								// Add song to queue
								addSongToQueue(play);
							} else {
								// Join a room
								var callback = function() {
									addSongToQueue(play);
								};
							    roomCodePopup(callback);
							}
							break;
						case 1:
							playlistsAction(play);
							break;
					}
					return true;
				}, 
				cancelText : 'Cancel'
			});
		}
		
		function favoriteSongActions() {
			
		}
		
		function searchSongActions(song) {
			
		}
	
		function playlistsAction(song) {
			$ionicActionSheet.show({
				buttons : _.map(playlists, function(playlist) {
					return {text: playlist.name, playlist: playlist};
				}),
				titleText : song.artist + ' - ' + song.title + '<br>Add to Playlist:',
				cancelText : 'Cancel',
				buttonClicked : function(index, button) {
					PlaylistsSvc.addSongToPlaylist(button.playlist, song).then(function(response) {
						// Success
					}, function(response) {
						// Error
					});
					return true;
				}
			});
		}
		
		function roomCodePopup(callback) {
            var $scope = $rootScope.$new();
            $scope.data = {};
	        var popup = $ionicPopup.show({
				template : '<label class="item item-input">' +
		        	'<input ng-model="data.roomCode" type="text" class="text-center" maxlength="4" placeholder="Room Code" capitalize>' +
		        	'</label>',
				title : 'Enter Room Code',
				subTitle : "It's on the screen in your room.",
				scope : $scope,
				buttons : [ 
					{
						text : 'Cancel'
					}, {
						text : '<b>OK</b>',
						type : 'button-positive',
						onTap : function(e) {
							if (!$scope.data.roomCode) {
								e.preventDefault();
							} else {
					        	QueueSvc.join($scope.data.roomCode).then(function() {
					        		if (callback != undefined) {
					        			callback();
					        		}
					        		return $scope.data.roomCode;
					        	}, function() {
									$ionicPopup.alert({
										title: "Error",
										template: 'Invalid Room Code'
									});
					        	});
							}
						}
					} 
				]
			});
	        return popup;
        }
		
        function addSongToQueue(song) {
			QueueSvc.addSongToQueue(queues[0], song).then(function(response) {
				// Success
			}, function(response) {
				// Error
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify(response.data)
				});
			});        	
        }		
	});
});
