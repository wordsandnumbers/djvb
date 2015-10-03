define([ 'angular' ], function(angular) {
	'use strict';

	/**
	 * @ngdoc function
	 * @name djvbApp.controller:QueueCtrl
	 * @description
	 * # QueueCtrl
	 * Controller of the djvbApp
	 */
	angular.module('djvbApp.controllers.QueueCtrl', [])
	.controller('QueueCtrl', function($ionicActionSheet, $ionicPopup, QueueSvc) {
		var vm = this;
		vm.queues = [];
		vm.selectPlay = selectPlay;
		
		QueueSvc.getQueuesList().then(function(queues) {
			vm.queues = queues;
		});
		
        function selectPlay(play) {
			var hideSheet = $ionicActionSheet.show({
				titleText : play.title + ' - ' + play.artist,
				destructiveText: 'Delete', 
				cancelText : 'Cancel',
				destructiveButtonClicked : function() {
					if (vm.queues.length > 0) {
						// Add song to queue
						QueueSvc.deletePlayFromQueue(vm.queues[0], play).then(function(response) {
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
					    //vm.roomCodeModal.show();
					}
					return true;
				}
			});
        }		
	});
});
