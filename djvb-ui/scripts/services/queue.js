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

		var queues, testq = [{
			"id": "5782623977c82ff56fc00126",
			"ownerId": "2927907626|00000000000000000000000000000000",
			"roomCode": "WWWF",
			"queued": [{
				"index": -1,
				"title": "Summerboy",
				"artist": "Lady Gaga",
				"duration": null,
				"position": null,
				"estimatedPlayTime": 1468187966943,
				"paused": false,
				"message": null,
				"locaton": null,
				"favorite": null,
				"id": 69001,
				"song_id": 69001,
				"play_id": "G-Test-f23c9167b37b-1468162624379-8",
				"message_color": null,
				"business_date": null,
				"enqueue_date": null,
				"start_date": null,
				"end_date": null
			}],
			"queue": [{
				"index": null,
				"title": "Start It Up",
				"artist": "Banks, Lloyd ft. Kanye West, Swizz Beatz, Ryan Leslie & Fabolous",
				"duration": null,
				"position": null,
				"estimatedPlayTime": null,
				"paused": false,
				"message": null,
				"locaton": null,
				"favorite": null,
				"id": 67519,
				"song_id": 67519,
				"play_id": "ace3d594-2177-43c8-9225-a6b19f7987c7",
				"message_color": null,
				"business_date": null,
				"enqueue_date": null,
				"start_date": null,
				"end_date": null
			}, {
				"index": null,
				"title": "That's What I Like About You",
				"artist": "Yearwood, Trisha",
				"duration": null,
				"position": null,
				"estimatedPlayTime": null,
				"paused": false,
				"message": null,
				"locaton": null,
				"favorite": null,
				"id": 1,
				"song_id": 1,
				"play_id": "ec906d54-5b48-4cab-9650-be6dd03cadf6",
				"message_color": null,
				"business_date": null,
				"enqueue_date": null,
				"start_date": null,
				"end_date": null
			}],
			"active": true,
			"session": {
				"session": "60502743-d0f1-4571-a1a5-874b6964b216",
				"email": "wordsandnumbers@gmail.com",
				"handle": "Duder",
				"color": "#70c1b3",
				"hide_handle_in_queue": false
			},
			"organization": "00000000000000000000000000000000",
			"mode": null,
			"queueInterval": null
		}, {
			"id": "5781dc9277c8c2ff1301ec87",
			"ownerId": "2927907626|00000000000000000000000000000000",
			"roomCode": "ABCD",
			"queued": [{
				"index": -1,
				"title": "Just Dropped In (To See What Condition My Condition Was In)",
				"artist": "Rogers, Kenny & The First Edition",
				"duration": null,
				"position": null,
				"estimatedPlayTime": null,
				"paused": false,
				"message": null,
				"locaton": null,
				"favorite": null,
				"id": 63530,
				"song_id": 63530,
				"play_id": "G-Test-f23c9167b37b-1468128404007-7",
				"message_color": null,
				"business_date": null,
				"enqueue_date": null,
				"start_date": null,
				"end_date": null
			}],
			"queue": [],
			"active": true,
			"session": {
				"session": "60502743-d0f1-4571-a1a5-874b6964b216",
				"email": "wordsandnumbers@gmail.com",
				"handle": "Duder",
				"color": "#ADACB5",
				"hide_handle_in_queue": false
			},
			"organization": "00000000000000000000000000000000",
			"mode": null,
			"queueInterval": null
		}, {
			"id": "5781dc9277c8c2ff1301ec87",
			"ownerId": "2927907626|00000000000000000000000000000000",
			"roomCode": "FFFF",
			"queued": [{
				"index": -1,
				"title": "Start It Up",
				"artist": "Banks, Lloyd ft. Kanye West, Swizz Beatz, Ryan Leslie & Fabolous",
				"duration": null,
				"position": null,
				"estimatedPlayTime": null,
				"paused": false,
				"message": null,
				"locaton": null,
				"favorite": null,
				"id": 67519,
				"song_id": 67519,
				"play_id": "ace3d594-2177-43c8-9225-a6b19f7987c7",
				"message_color": null,
				"business_date": null,
				"enqueue_date": null,
				"start_date": null,
				"end_date": null
			}],
			"queue": [{
				"index": null,
				"title": "That's What I Like About You",
				"artist": "Yearwood, Trisha",
				"duration": null,
				"position": null,
				"estimatedPlayTime": null,
				"paused": false,
				"message": null,
				"locaton": null,
				"favorite": null,
				"id": 1,
				"song_id": 1,
				"play_id": "ec906d54-5b48-4cab-9650-be6dd03cadf6",
				"message_color": null,
				"business_date": null,
				"enqueue_date": null,
				"start_date": null,
				"end_date": null
			}],
			"active": true,
			"session": {
				"session": "60502743-d0f1-4571-a1a5-874b6964b216",
				"email": "wordsandnumbers@gmail.com",
				"handle": "Duder",
				"color": "#ADACB5",
				"hide_handle_in_queue": false
			},
			"organization": "00000000000000000000000000000000",
			"mode": null,
			"queueInterval": null
		}];
		
		pollQueues();
		
		return {
			createPopup: createPopup,
			join: join, 
			getQueues: getQueues, 
			getQueuesList: getQueuesList, 
			addSongToQueue: addSongToQueue, 
			deletePlayFromQueue: deletePlayFromQueue, 
			reorderQueuePlay: reorderQueuePlay, 
			deleteQueue: deleteQueue, 
			updateQueued: updateQueued, 
			setLights: setLights,
			songInQueue: songInQueue
		}

		function createPopup(roomCode, message) {
			return $q(function (resolve, reject) {
				$http.post('/api/v1/queue/popup/' + roomCode, message).then(function(response) {
					resolve(response.data);
				}, function(errorResponse) {
					reject(errorResponse);
				});
			});
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
		
		function songInQueue(song) {
			return _.find((queues[0] || {}).queue, {id: song.id});
		}
		
		function spliceQueue(oldQueue, newQueue) {
			// Replace arrays of songs using splice to preserve array references
			oldQueue.queue.splice.apply(oldQueue.queue, [0, oldQueue.queue.length].concat(newQueue.queue));
			oldQueue.queued.splice.apply(oldQueue.queued, [0, oldQueue.queued.length].concat(newQueue.queued));
		}
	});
});
