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
    .controller('FavoritesCtrl', function ($scope, PlaylistsSvc, ActionSheetSvc) {
		var vm = this;
		vm.favorites = {};
		vm.hasMoreData = hasMoreData;
		vm.selectSong = selectSong;
		vm.nextPage = nextPage;
		vm.moreDataCanBeLoaded = moreDataCanBeLoaded;

		PlaylistsSvc.getFavoritesList().then(function(favorites) {
			vm.favorites = favorites;
		})
		
		function selectSong(song) {
			ActionSheetSvc.playHistoryActions(song);
		}

		function nextPage() {
			PlaylistsSvc.nextPage(vm.favorites).then(function (response) {
				vm.favorites = response;
				$scope.$broadcast('scroll.infiniteScrollComplete');
			});
		}
		
		function moreDataCanBeLoaded() {
			return false;
		}
		
		function hasMoreData() {
			return vm.favorites.page < vm.favorites.total_pages;
		}

    });
});
