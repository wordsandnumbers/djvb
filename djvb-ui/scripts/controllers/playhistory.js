define([ 'angular' ], function(angular) {
	'use strict';

	/**
	 * @ngdoc function
	 * @name djvbApp.controller:PlayHistoryCtrl
	 * @description
	 * # PlayHistoryCtrl
	 * Controller of the djvbApp
	 */
	angular.module('djvbApp.controllers.PlayHistoryCtrl', [])
	.controller('PlayHistoryCtrl', function($scope, PlaylistsSvc, ActionSheetSvc) {
		var vm = this;
		vm.history = {};
		vm.hasMoreData = hasMoreData;
		vm.selectPlay = selectPlay;
		vm.nextPage = nextPage;

		PlaylistsSvc.getPlayHistoryList().then(function(history) {
			vm.history = history;
			vm.playGroups = _.groupBy(vm.history.plays, function (play) {
				return play.business_date;
			});
		});

		function selectPlay(play) {
			ActionSheetSvc.playHistoryActions(play);
		}
		
		function nextPage() {
			PlaylistsSvc.nextPage(vm.history).then(function (response) {
				vm.history = response;
				vm.playGroups = _.groupBy(vm.history.plays, function (play) {
					return play.business_date;
				});
				$scope.$broadcast('scroll.infiniteScrollComplete');
			});
		}
		
		function hasMoreData() {
			return vm.history.page < vm.history.total_pages;
		}
	});
});
