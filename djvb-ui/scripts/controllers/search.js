define(['angular', 'lodash'], function (angular, _) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:SearchCtrl
   * @description
   * # SearchCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.SearchCtrl', [])
  .directive('focusMe', function($timeout) {
	  return {
	    scope: { trigger: '=focusMe' },
	    link: function(scope, element) {
	      scope.$watch('trigger', function(value) {
	        if(value === true) { 
	          //console.log('trigger',value);
	          $timeout(function() {
	            element[0].focus();
	            scope.trigger = false;
	          });
	        }
	      });
	    }
	  };
  })
  .controller('SearchCtrl', function ($scope, $log, $http, $ionicScrollDelegate, $ionicLoading, $ionicActionSheet, $ionicModal, $ionicPopup, QueueSvc, PlaylistsSvc) {
        var vm = this;
        vm.searchString = '';
        vm.songGroups = {};
        vm.recentSearches = [];
        vm.noResults = false;
        vm.search = search;
        vm.exactSearch = exactSearch;
        vm.hasSongGroups = hasSongGroups;
        vm.clearSearch = clearSearch;
        vm.selectSong = selectSong;
        vm.searchComplete = false;
        vm.queues = [];
        vm.playlists;
        vm.joinRoom = joinRoom;
        
        QueueSvc.getQueues().then(function(queues) {
        	vm.queues = queues;
        })

        PlaylistsSvc.getPlaylistsList().then(function(playlists) {
        	vm.playlists = playlists;
        })

		$ionicModal.fromTemplateUrl('views/roomcodemodal.html', {
			scope: $scope,
			animation: 'slide-in-up'
		}).then(function(modal) {
			vm.roomCodeModal = modal;
		});
        
        function exactSearch(searchString) {
            search('"' + searchString + '"');
        }
      
        function search(searchString) {
        	vm.searchString = searchString;
            
            if (!_.isEmpty(searchString)) { 
            	
                $ionicLoading.show({
                    delay: 500, 
                    noBackdrop: true, 
                    template: 'Loading...'
                });

                $http.get('/api/v1/songs/query', {
                    params: {
                        query: searchString,
                        per_page: 25,
                        page: 1
                    }
                }).then(function(response) {
                    vm.songGroups = _.groupBy(response.data.songs, 'artist');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                    if (hasSongGroups()) {
                    	if (_.indexOf(vm.recentSearches, searchString)===-1) {
                    		vm.recentSearches.unshift(searchString); 
                    	}
                    	vm.noResults = false;
                    	vm.searchComplete = true;
                    } else {
                    	vm.noResults = true;
                    }
                }, function(error) {
                    $log.error(error);
                    $ionicLoading.hide();
                });
            }
        }
        
        function selectSong(song) {
			var hideSheet = $ionicActionSheet.show({
				buttons : [{
					text : '<strong>Sing Now!</strong>'
				}, {
					text : 'Add to Playlist'
				}],
				titleText : song.artist + ' - ' + song.title,
				cancelText : 'Cancel',
				buttonClicked : function(index) {
					switch (index) {
						case 0:
							if (vm.queues.length > 0) {
								// Add song to queue
								QueueSvc.addSongToQueue(vm.queues[0], song).then(function(response) {
									// Success
								}, function(config) {
									// Error
									$ionicPopup.alert({
										title: "Error",
										template: JSON.stringify(config.data)
									});
								});
							} else {
								// Join a room
							    vm.roomCodeModal.show();
							}
							break;
						case 1:
							$ionicActionSheet.show({
								buttons : _.map(vm.playlists, function(playlist) {
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
							})
							break;
					}
					return true;
				}
			});
        }
        
        function joinRoom(roomCode) {
        	QueueSvc.join(roomCode);
        }
        
        function clearSearch() {
        	vm.searchString = '';
        	vm.songGroups = {};
        	vm.noResults = false;
        }
        
        function hasSongGroups() {
        	return _.keys(vm.songGroups).length > 0 ? true : false;
        }
    });
});
