define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:PlaylistsCtrl
   * @description
   * # PlaylistsCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.PlaylistsCtrl', [])
    .controller('PlaylistsCtrl', function ($scope, PlaylistsSvc, $ionicPopup) {
    	var vm = this;
    	vm.playlists;
    	vm.newPlaylistPopup = newPlaylistPopup;
		vm.refresh = refresh;
		
    	function newPlaylistPopup() {
	        var popup = $ionicPopup.show({
				template : '<label class="item item-input">' +
		        	'<input ng-model="vm.name" type="text" placeholder="Playlist Name">' +
		        	'</label>',
				title : 'Create Playlist',
				scope : $scope,
				buttons : [ 
					{
						text : 'Cancel'
					}, {
						text : '<b>OK</b>',
						type : 'button-positive',
						onTap : function(e) {
							if (!$scope.vm.name) {
								e.preventDefault();
							} else {
					        	PlaylistsSvc.createPlaylist($scope.vm.name).then(function() {
					        		$scope.vm.name = '';
					        		return $scope.vm.name;
					        	}, function() {
									$ionicPopup.alert({
										title: 'Error',
										template: 'Couldn\'t create playlist.'
									});
					        	});
							}
						}
					} 
				]
			});
	        return popup;
        }
    	
    	PlaylistsSvc.getPlaylistsList().then(function(playlists) {
        	vm.playlists = playlists;
        })

		function refresh() {
			PlaylistsSvc.getPlaylists().finally(function () {
				$scope.$broadcast('scroll.refreshComplete');
			})
		}
	});
});
