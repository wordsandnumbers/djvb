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
		
		var playlists = [];
		
		return {
			getPlaylistsList: getPlaylistsList, 
			getPlaylists: getPlaylists, 
			getPlaylist: getPlaylist,
			updatePlaylist: updatePlaylist, 
			addSongToPlaylist: addSongToPlaylist
		}

		function getPlaylists() {
			return $q(function(resolve, reject) {
				$http.get('/api/v1/playlists/').then(function(response) {
					// Let's fix the response so it's an array. Will fix server later.
					var playlists = _.map(_.keys(response.data.lists), function(key) {
						var playlist = response.data.lists[key];
						playlist.name = key;
						return playlist;
					});
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
		
		function getPlaylist(name) {
			return $q(function(resolve, reject) {
				$http.get('/api/v1/playlists/' + name).then(function(response) {
					playlists[name] = response.data;
					resolve(playlists[name]);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function updatePlaylist(playlist) {
			return $q(function(resolve, reject) {
				$http.put('/api/v1/playlists/' + playlist.name, playlist).then(function(response) {
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
	});
});
