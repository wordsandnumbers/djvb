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
	.controller('PlayHistoryCtrl', function(PlaylistsSvc, ActionSheetSvc) {
		var vm = this;
		vm.history = [];
		vm.selectPlay = selectPlay;

		PlaylistsSvc.getPlayHistoryList().then(function(history) {
			vm.history = history;
		})

		function selectPlay(play) {
			ActionSheetSvc.playHistoryActions(play);
		}
	});
});
