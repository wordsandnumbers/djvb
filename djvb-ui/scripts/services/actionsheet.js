define(['angular', 'lodash'], function (angular, _) {
  'use strict';

  /**
   * @ngdoc service
   * @name djvbApp.ActionSheetSvc
   * @description
   * # ActionSheetSvc
   * Service in the djvbApp.
   */
  angular.module('djvbApp.services.ActionSheetSvc', [])
	.service('ActionSheetSvc', function (
		$rootScope, 
		$ionicLoading, 
		$ionicPopup, 
		$ionicActionSheet, 
		constants, 
		UserSvc, 
		PlaylistsSvc, 
		QueueSvc, 
		$timeout,
		$state
	) {
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
			roomCodePopup: roomCodePopup,
    		searchSongActions: browseSongActions, 
    		browseSongActions: browseSongActions
		};
		
		function playlistSongActions(playlist, song) {
			$ionicActionSheet.show({
				titleText : song.title + ' - ' + song.artist, 
				buttons: [
					{text: '<strong>Sing Now!</strong>'}, 
					{text: 'Add to Playlist'},
					{text: 'Browse Artist'}
				], 
				buttonClicked : function(index) {
					switch (index) {
						case 0:
							queueAction(song);
							break;
						case 1:
							playlistsAction(song);
							break;
						case 2:
							$state.go('tabs.browsequery', {mode: 'artist', query: song.artist});
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
					{text: 'Add to Playlist'},
					{text: 'Browse Artist'}
				], 
				buttonClicked : function(index) {
					switch (index) {
						case 0:
							queueAction(play);
							break;
						case 1:
							playlistsAction(play);
							break;
						case 2:
							$state.go('tabs.browsequery', {mode: 'artist', query: play.artist});
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

		function browseSongActions(song) {
			$ionicActionSheet.show({
				titleText : song.title + ' - ' + song.artist, 
				buttons: getButtons(song), 
				buttonClicked : function(index) {
					switch (index) {
						case 0:
							queueAction(song);
							break;
						case 1:
							playlistsAction(song);
							break;
						case 2:
							tagsAction(song);
							break;
					}
					return true;
				}, 
				cancelText : 'Cancel'
			});
		}
		
		function getButtons(song) {
			var buttons = [
               {text: '<strong>Sing Now!</strong>'}, 
               {text: 'Add to Playlist'}
			];
			if (!_.isEmpty(song.tags)) {
				buttons.push({text: 'Browse Tags'});
			}
			return buttons;
		}
		
		function playlistsAction(song) {
			$ionicActionSheet.show({
				buttons : _.sortBy(_.map(playlists, function(playlist) {
					return {text: playlist.name, playlist: playlist};
				}), 'text'),
				titleText : song.artist + ' - ' + song.title + '<br>Add to Playlist:',
				cancelText : 'Cancel',
				buttonClicked : function(index, button) {
					PlaylistsSvc.addSongToPlaylist(button.playlist, song).then(function(response) {
						// Success			    
						$ionicLoading.show({
					        template: '<p>Added to playlist!</p><i class="icon ion-checkmark-round message-icon"></i>',
					        noBackdrop: true,
					        duration: 1500
					    });

					}, function(response) {
						// Error
					});
					return true;
				}
			});
		}
		
		function queueAction(song) {
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
		}
		
		function tagsAction(song) {
			var sortedTags = _.sortBy(song.tags);
			$ionicActionSheet.show({
				buttons : _.map(sortedTags, function(tag) {
					return {text: tag};
				}),
				titleText : 'Browse Tags:',
				cancelText : 'Cancel',
				buttonClicked : function(index, button) {
                    $state.go('tabs.browsequery', {mode: 'tag', query: sortedTags[index]});
					return true;
				}
			});
		}
		
		function roomCodePopup(callback) {
            var $scope = $rootScope.$new();
	        var popup = $ionicPopup.show({
				templateUrl : constants.resourcesBaseUrl + '/views/roomcodepopup.html',
				title : 'Enter Room Code',
				subTitle : "It's on the screen in your room.",
				scope : $scope
			});
	        $scope.disabled = false;
			$scope.popup = popup;
			$scope.data = {};
			$scope.joinRoom = function () {
				$scope.disabled = true;
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
				}).finally(function () {
					$scope.popup.close();
					$scope.disabled = false;
				});
			}
			
			return popup;
        }
		
        function addSongToQueue(song) {
			QueueSvc.addSongToQueue(queues[0], song).then(function(response) {
				// Success
			    $ionicLoading.show({
			        template: '<p>Song Added!</p><i class="icon ion-checkmark-round message-icon"></i>',
			        noBackdrop: true,
			        duration: 1500
			    });
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
