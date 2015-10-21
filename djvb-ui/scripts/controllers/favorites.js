define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:FavoritesCtrl
   * @description
   * # FavoritesCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.FavoritesCtrl', [])
    .controller('FavoritesCtrl', function (PlaylistsSvc, ActionSheetSvc) {
		var vm = this;
		vm.favorites = [];
		vm.selectSong = selectSong;

		PlaylistsSvc.getFavoritesList().then(function(favorites) {
			vm.favorites = favorites;
		})
		
		function selectSong(song) {
			ActionSheetSvc.playlistSongActions(song);
		}

    });
});
