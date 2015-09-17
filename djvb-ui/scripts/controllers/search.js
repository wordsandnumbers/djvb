define(['angular', 'lodash'], function (angular, _) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:SearchCtrl
   * @description
   * # SearchCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.SearchCtrl', [])
    .controller('SearchCtrl', function ($scope, $log, $http, $ionicScrollDelegate, $ionicLoading, $ionicActionSheet) {
        var vm = this;
        vm.searchString = '';
        vm.songGroups = {};
        vm.recentSearches = [];
        vm.noResults = false;
        vm.search = search;
        vm.exactSearch = exactSearch;
        vm.hasSongGroups = hasSongGroups;
        vm.clearSearch = clearSearch;
        vm.selectSong = selectSong;
            
        function exactSearch(searchString) {
            search('"' + searchString + '"');
        }
      
        function search(searchString) {
        	vm.searchString = searchString;
            
            if (!_.isEmpty(searchString)) { 
            	
                $ionicLoading.show({
                    delay: 500, 
                    noBackdrop: true, 
                    template: 'Loading...'
                });

                $http.get('/api/v1/songs/query', {
                    params: {
                        query: searchString,
                        per_page: 25,
                        page: 1
                    }
                }).then(function(response) {
                    vm.songGroups = _.groupBy(response.data.songs, 'artist');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                    if (hasSongGroups()) {
                    	if (_.indexOf(vm.recentSearches, searchString)===-1) {
                    		vm.recentSearches.unshift(searchString); 
                    	}
                    	vm.noResults = false;
                    } else {
                    	vm.noResults = true;
                    }
                }, function(error) {
                    $log.error(error);
                    $ionicLoading.hide();
                });
            }
        }
        
        function selectSong(song) {
			var hideSheet = $ionicActionSheet.show({
				buttons : [{
					text : '<strong>Sing Now!</strong>'
				}, {
					text : 'Add to Playlist'
				}],
				titleText : song.artist + ' - ' + song.title,
				cancelText : 'Cancel',
				buttonClicked : function(index) {
					$ionicActionSheet.show({
						buttons : [{
							text : '<strong>stuff!</strong>'
						}],
						titleText : song.artist + ' - ' + song.title + '<br>Add to Playlist:',
						cancelText : 'Cancel',
						buttonClicked : function(index) {
							return true;
						}
					})
					return true;
				}
			});
        }
        
        function clearSearch() {
        	vm.searchString = '';
        	vm.songGroups = {};
        	vm.noResults = false;
        }
        
        function hasSongGroups() {
        	return _.keys(vm.songGroups).length > 0 ? true : false;
        }
    });
});
