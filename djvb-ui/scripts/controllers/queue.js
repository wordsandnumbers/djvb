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
	.controller('QueueCtrl', function($scope, $ionicActionSheet, $ionicLoading, $ionicPopup, $ionicModal, QueueSvc, constants) {
		var vm = this;
		vm.queues = [];
		vm.showReorder = false;
		vm.level = null;
		vm.popupModal = popupModal;
		vm.createPopup = createPopup;
		vm.selectPlay = selectPlay;
		vm.showSettings = showSettings;
		vm.deleteQueue = deleteQueue;
		vm.refresh = refresh;
		vm.reorderPlay = reorderPlay;
		vm.setLights = setLights;
		
		QueueSvc.getQueuesList().then(function(queues) {
			vm.queues = queues;
		});

		$ionicModal.fromTemplateUrl(constants.resourcesBaseUrl + '/views/queuesettingsmodal.html', {
			scope: $scope,
			animation: 'slide-in-up'
		}).then(function(modal) {
			vm.queueSettingsModal = modal;
		});

		$ionicModal.fromTemplateUrl(constants.resourcesBaseUrl + '/views/createpopupmodal.html', {
			scope: $scope,
			animation: 'slide-in-up'
		}).then(function(modal) {
			vm.createPopupModal = modal;
		});

		function createPopup() {
			QueueSvc.createPopup(vm.message).then(function () {
				$ionicLoading.show({
					template: '<p>Message sent!</p><i class="icon ion-checkmark-round message-icon"></i>',
					noBackdrop: true,
					duration: 1500
				});
				vm.message = "";
			}, function(response) {
				// Error
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify(response.data)
				});
			});
		}
		
		function popupModal() {
			vm.createPopupModal.show();
		}
		
        function selectPlay(play) {
			var hideSheet = $ionicActionSheet.show({
				buttons : vm.queues[0].queued.length > 0 ? [{text:'Swap for Next Song'}] : [],
				titleText : play.title + ' - ' + play.artist,
				destructiveText: 'Delete', 
				cancelText : 'Cancel',
				destructiveButtonClicked : function() {
					if (vm.queues.length > 0) {
						// Add song to queue
						QueueSvc.deletePlayFromQueue(vm.queues[0], play).then(function(response) {
							// Success
						}, function(response) {
							// Error
							$ionicPopup.alert({
								title: "Error",
								template: JSON.stringify(response.data)
							});
						});
					} else {
						// Join a room
					    //vm.roomCodeModal.show();
					}
					return true;
				},
				buttonClicked : function(index) {
					switch (index) {
						case 0:
							QueueSvc.updateQueued(vm.queues[0], play).then(function() {
								
							}, function(response) {
								// Error
								$ionicPopup.alert({
									title: "Error",
									template: JSON.stringify(response.data)
								});
							});
							break;
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

		function refresh() {
			QueueSvc.getQueues().finally(function () {
				$scope.$broadcast('scroll.refreshComplete');
			})
		}

		function reorderPlay(play, fromIndex, toIndex) {
        	QueueSvc.reorderQueuePlay(vm.queues[0], play, fromIndex, toIndex).then(function(response) {
        		// Success
        		// TODO: highlight moved item?
        	}, function(response) {
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify("Couldn't reorder queue.")
				});        		
        	})
        }
        
        function setLights(level) {
    		vm.level = level;
        	QueueSvc.setLights(vm.queues[0], level).then(function() {
        		vm.queueSettingsModal.hide();
        	}, function(response) {
        		vm.level = null;
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify("Couldn't set lights.")
				});        		
        	});
        }
	});
});
