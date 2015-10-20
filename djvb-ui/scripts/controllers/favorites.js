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
    .controller('FavoritesCtrl', function (PlaylistsSvc) {
		var vm = this;
		vm.favorites = [];

		PlaylistsSvc.getFavorites().then(function(favorites) {
			vm.favorites = favorites;
		})

    });
});
