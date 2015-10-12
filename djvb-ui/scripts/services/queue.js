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
	.service('QueueSvc', function ($http, $q) {

		var queues;
		
		return {
			join: join, 
			getQueues: getQueues, 
			getQueuesList: getQueuesList, 
			addSongToQueue: addSongToQueue, 
			deletePlayFromQueue: deletePlayFromQueue, 
			updateQueue: updateQueue, 
			deleteQueue: deleteQueue, 
			updateQueued: updateQueued, 
			setLights: setLights
		}
		
		function join(roomCode) {
			return $q(function(resolve, reject) {
				$http.post('/api/v1/queue/join', {'roomCode': roomCode}).then(function(response){
					resolve(queues.unshift(response.data));
				}, function(error) {
					reject(error);
				});
			});	
		}
		
		function getQueues() {
			return $q(function(resolve, reject) {
				$http.get('/api/v1/queue/queues').then(function(config){
					queues = config.data;
					resolve(queues);
				}, function(error) {
					reject(error);
				});
			});
		}
		
		function getQueuesList() {
			return $q(function(resolve, reject) {
				if (queues === undefined) {
					getQueues().then(function (config) {
						resolve(config);
					}, function(config) {
						reject(config);
					});
				} else {
					resolve(queues);
				}
			});
		}
		
		function addSongToQueue(queue, song) {
			return $q(function(resolve, reject) {
				$http.post('/api/v1/queue/queue', {'room_code':queue.roomCode, 'song_id':song.id}).then(function(config){
					// Replace array of songs using splice to preserve array reference
					Array.prototype.splice.apply(queues[0].queue, [0, config.data.queue.length].concat(config.data.queue));
					resolve(queues[0]);
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
				}).then(function(config){
					// Replace array of songs using splice to preserve array reference
					Array.prototype.splice.apply(queues[0].queue, [0, config.data.queue.length].concat(config.data.queue));
					resolve(queues[0]);
				}, function(error) {
					reject(error);
				});
			});			
		}
		
		function updateQueue(queue, song, fromIndex, toIndex) {
			return $q(function(resolve, reject) {
				$http.put('/api/v1/queue/queue', {
					'room_code':queue.roomCode, 
					'song_id':queue.queue[0].id
				}).then(function(response){
					queue = response.data;
					resolve(queue);
				}, function(response) {
					reject(response);
				});
			});	
		}
		
		function updateQueued(queued) {
			
		}
		
		function deleteQueue(queue) {
			return $q(function(resolve, reject) {
				$http.delete('/api/v1/queue/uq/' + queue.id).then(function(response) {
					resolve(response);
				}, function(response) {
					reject(response);
				});
			});
		}
		
		function setLights() {
			
		}
	});
});
