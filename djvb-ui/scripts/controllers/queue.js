define([ 'angular' ], function(angular) {
	'use strict';

	/**
	 * @ngdoc function
	 * @name djvbApp.controller:QueueCtrl
	 * @description
	 * # QueueCtrl
	 * Controller of the djvbApp
	 */
	angular.module('djvbApp.controllers.QueueCtrl', ['ionic'])
	.controller('QueueCtrl', function($scope, $ionicActionSheet, $ionicPopup, $ionicModal, QueueSvc) {
		var vm = this;
		vm.queues = [];
		vm.selectPlay = selectPlay;
		vm.showSettings = showSettings;
		vm.deleteQueue = deleteQueue;
		
		QueueSvc.getQueuesList().then(function(queues) {
			vm.queues = queues;
		});

		$ionicModal.fromTemplateUrl('views/queuesettingsmodal.html', {
			scope: $scope,
			animation: 'slide-in-up'
		}).then(function(modal) {
			vm.queueSettingsModal = modal;
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
        
        function showSettings() {
        	vm.queueSettingsModal.show();
        }
        
        function deleteQueue(queue) {
        	QueueSvc.deleteQueue(vm.queues[0]).then(function(response) {
        		vm.queueSettingsModal.hide();
        	}, function(response) {
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify(response.data)
				});        		
        	})
        }
	});
});
