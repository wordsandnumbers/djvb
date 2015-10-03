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
		
		var playlists = {};
		
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
					angular.extend(playlists, response.data.lists);
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
		
		function updatePlaylist(name) {
			return $q(function(resolve, reject) {
				$http.put('/api/v1/playlists/' + name, playlists[name]).then(function(response) {
					Array.prototype.splice.apply(playlists[name].songs, [0, playlists[name].songs.length].concat(response.data.songs));
					resolve(playlists[name]);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function addSongToPlaylist(playlistKey, song) {
			return $q(function(resolve, reject) {
				playlists[playlistKey].songs.push(song);
				updatePlaylist(playlistKey, playlists[playlistKey].songs).then(function (response) {
					resolve(response);
				}, function(response) {
					reject(response);
				});
			});
		}
	});
});
