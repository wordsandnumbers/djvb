define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:PlaylistCtrl
   * @description
   * # PlaylistCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.PlaylistCtrl', [])
    .controller('PlaylistCtrl', function ($stateParams, PlaylistsSvc) {
    	var vm = this;
    	
    	PlaylistsSvc.getPlaylistsList().then(function (response) {
    		vm.playlists = response;
    		vm.playlist = _.find(vm.playlists, {id: $stateParams.playlistId});
    	})
    });
});
