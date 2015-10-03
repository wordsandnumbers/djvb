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
    .controller('PlaylistsCtrl', function (PlaylistsSvc) {
    	var vm = this;
    	vm.playlists;
    	
    	PlaylistsSvc.getPlaylistsList().then(function(playlists) {
        	vm.playlists = playlists;
        })
    });
});
