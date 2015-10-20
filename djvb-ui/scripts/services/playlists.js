define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc service
   * @name djvbApp.PlaylistsSvc
   * @description
   * # PlaylistsSvc
   * Service in the djvbApp.
   */
  angular.module('djvbApp.services.PlaylistsSvc', [])
	.service('PlaylistsSvc', function ($http, $q) {
		
		var playlists = [],
			favorites = [], 
			history = []
		
		return {
			getPlaylistsList: getPlaylistsList, 
			getPlaylists: getPlaylists, 
			getPlaylist: getPlaylist, 
			createPlaylist: createPlaylist, 
			updatePlaylist: updatePlaylist, 
			addSongToPlaylist: addSongToPlaylist, 
			deleteSongFromPlaylist: deleteSongFromPlaylist, 
			getFavorites: getFavorites, 
			getPlayHistory: getPlayHistory
		}

		function getPlaylists() {
			return $q(function(resolve, reject) {
				$http.get('/api/v1/playlists/').then(function(response) {
					playlists = response.data.lists;
					resolve(playlists);
				}, function(response) {
					reject(response);
				})
			});
		}
		
		function getPlaylistsList() {
			return $q(function(resolve, reject) {
				if (_.isEmpty(playlists)) {
					getPlaylists().then(function (response) {
						resolve(response);
					}, function(response) {
						reject(response);
					});
				} else {
					resolve(playlists);
				}
			});			
		}
		
		function getPlaylist(playlistId) {
			return $q(function(resolve, reject) {
				$http.get('/api/v1/playlists/' + playlistId).then(function(response) {
					playlists[name] = response.data;
					resolve(playlists[name]);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function createPlaylist(name) {
			return $q(function(resolve, reject) {
				$http.post('/api/v1/playlists/', {'name': name, 'songs': []}).then(function(response) {
					playlists.push(response.data);
					resolve(response.data);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function updatePlaylist(playlist) {
			return $q(function(resolve, reject) {
				$http.put('/api/v1/playlists/', playlist).then(function(response) {
					var foundPlaylist = _.find(playlists, {id: playlist.id});
					Array.prototype.splice.apply(foundPlaylist.songs, [0, foundPlaylist.songs.length].concat(response.data.songs));
					resolve(foundPlaylist);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function addSongToPlaylist(playlist, song) {
			return $q(function(resolve, reject) {
				playlist.songs.push(song);
				updatePlaylist(playlist).then(function (response) {
					resolve(response);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function deleteSongFromPlaylist(playlist, index) {
			return $q(function(resolve, reject) {
				playlist.songs.splice(index, 1);
				updatePlaylist(playlist).then(function (response) {
					resolve(response);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function getFavorites() {
			return $q(function(resolve, reject) {
				$http.get('/api/v1/songs/favorites').then(function(response) {
					favorites = response.data;
					resolve(favorites);
				}, function(response) {
					reject(response);
				})
			});
		}
		
		function getPlayHistory() {
			return $q(function(resolve, reject) {
				$http.get('/api/v1/songs/playHistory').then(function(response) {
					history = response.data;
					resolve(history);
				}, function(response) {
					reject(response);
				})
			});
		}
	});
});
