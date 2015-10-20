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
	.controller('PlayHistoryCtrl', function(PlaylistsSvc) {
		var vm = this;
		vm.history = [];

		PlaylistsSvc.getPlayHistory().then(function(history) {
			vm.history = history;
		})

	});
});
