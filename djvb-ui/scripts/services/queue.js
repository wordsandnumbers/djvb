define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc service
   * @name djvbApp.Queue
   * @description
   * # Queue
   * Service in the djvbApp.
   */
  angular.module('djvbApp.services.QueueSvc', [])
	.service('QueueSvc', function ($http, $q, $timeout) {

		var queues;
		
		pollQueues();
		
		return {
			join: join, 
			getQueues: getQueues, 
			getQueuesList: getQueuesList, 
			addSongToQueue: addSongToQueue, 
			deletePlayFromQueue: deletePlayFromQueue, 
			reorderQueuePlay: reorderQueuePlay, 
			deleteQueue: deleteQueue, 
			updateQueued: updateQueued, 
			setLights: setLights
		}
		
        function pollQueues() {
        	getQueues();
            // Get queues every 15 seconds
            $timeout(pollQueues, 15000);
        }

        function join(roomCode) {
			return $q(function(resolve, reject) {
				$http.post('/api/v1/queue/join', {'roomCode': roomCode}).then(function(response){
					queues.unshift(response.data);
					resolve(response.data);
				}, function(error) {
					reject(error);
				});
			});	
		}
		
		function getQueues() {
			return $q(function(resolve, reject) {
				$http.get('/api/v1/queue/queues').then(function(response){
					if (queues === undefined) {
						queues = response.data;
					} else {
	                    // Use `splice` to replace array members so the original array reference is maintained
						_.forEach(response.data, function(queue) {
							var queueIndex = _.findIndex(queues, {'id': queue.id});
							if (queueIndex > -1) {
								spliceQueue(queues[queueIndex], queue);
							} else {
								queues.push(queue);
							}
						})
					}
					resolve(queues);
				}, function(error) {
					reject(error);
				});
			});
		}
		
		function getQueuesList() {
			return $q(function(resolve, reject) {
				if (queues === undefined) {
					getQueues().then(function (response) {
						resolve(response);
					}, function(response) {
						reject(response);
					});
				} else {
					resolve(queues);
				}
			});
		}
		
		function addSongToQueue(queue, song) {
			return $q(function(resolve, reject) {
				$http.post('/api/v1/queue/queue', {'room_code':queue.roomCode, 'song_id':song.id}).then(function(response){
					spliceQueue(queue, response.data);
					resolve(queue);
				}, function(error) {
					reject(error);
				});
			});
		}
		
		function deletePlayFromQueue(queue, play) {
			return $q(function(resolve, reject) {
				$http.delete('/api/v1/queue/queue', {
					data: {
						'room_code': queue.roomCode, 
						'playId':play.play_id,
						'from':_.indexOf(_.pluck(queue.queue, 'play_id'),play.play_id)
					},
					headers: {
						'Content-Type': 'application/json'
					}
				}).then(function(response){
					spliceQueue(queue, response.data);
					resolve(queue);
				}, function(response) {
					reject(response);
				});
			});			
		}
		
		function reorderQueuePlay(queue, play, fromIndex, toIndex) {
			return $q(function(resolve, reject) {
				$http.put('/api/v1/queue/queue', {
					'room_code':queue.roomCode, 
					'playId':play.play_id,
					'from': fromIndex, 
					'to': toIndex
				}).then(function(response){
					spliceQueue(queue, response.data);
					resolve(queue);
				}, function(response) {
					reject(response);
				});
				// Swap out the items before the response comes back.
				queue.queue[fromIndex] = queue.queue.splice(toIndex, 1, queue.queue[fromIndex])[0];
			});	
		}
		
		function updateQueued(queue, play) {
			return $q(function(resolve, reject) {
				$http.put('/api/v1/queue/queued', {
					'room_code': queue.roomCode, 
					'song_id': play.song_id,
					'to': queue.queued[0].play_id
				}).then(function(response){
					var responseQueue = response.data;
					var queueIndex = _.findIndex(queues, {'id': responseQueue.id});
					if (queueIndex > -1) {
						spliceQueue(queues[queueIndex], responseQueue);
					} else {
						queues.push(responseQueue);
					}
					resolve(responseQueue);
				}, function(response) {
					reject(response);
				});
			});	
		}
		
		function deleteQueue(queue) {
			return $q(function(resolve, reject) {
				$http.delete('/api/v1/queue/uq/' + queue.id).then(function(response) {
					var foundIndex = _.findIndex(queues, {'id': queue.id});
					if (foundIndex > -1) {
						queues.splice(foundIndex, 1);
					}
					resolve(response);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function setLights(queue, level) {
			// Level: [0, 1, 2]
			return $q(function(resolve, reject) {
				$http.get('/api/v1/queue/lights/' + queue.roomCode + '/' + level).then(function(response){
					resolve(response);
				}, function(response) {
					reject(response);
				});
			});	
		}
		
		function spliceQueue(oldQueue, newQueue) {
			// Replace arrays of songs using splice to preserve array references
			oldQueue.queue.splice.apply(oldQueue.queue, [0, oldQueue.queue.length].concat(newQueue.queue));
			oldQueue.queued.splice.apply(oldQueue.queued, [0, oldQueue.queued.length].concat(newQueue.queued));
		}
	});
});
