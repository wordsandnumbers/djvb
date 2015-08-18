define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:SearchCtrl
   * @description
   * # SearchCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.SearchCtrl', [])
    .controller('SearchCtrl', function ($scope, $log, $http, $ionicScrollDelegate, $ionicLoading) {
        var vm = this;
        vm.search = search;
        vm.exactSearch = exactSearch;
        vm.searchString = '';
        vm.songGroups = {};
            
        function exactSearch(searchString) {
            search('"' + searchString + '"');
        }
      
        function search(searchString) {
/*            $http.get('http://djvoxbox.herokuapp.com/app/song/query/' + vm.searchString).then(function(results) {
                vm.songs = results;
            });*/
            
            if (!_.isEmpty(searchString)) { 
                $ionicLoading.show({
                    delay: 500, 
                    noBackdrop: true, 
                    template: 'Loading...'
                });

                $http.get('http://vbsongs.com/api/v1/songs/search.json', {
                    params: {
                        query: searchString,
                        per_page: 100,
                        page: 1
                    }
                }).then(function(response) {
                    vm.songGroups = _.groupBy(response.data.songs, 'artist');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, function(error) {
                    $log.error(error);
                    $ionicLoading.hide();
                });
            }
        }
    });
});
