define(['angular', 'lodash'], function (angular, _) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:SearchCtrl
   * @description
   * # SearchCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.SearchCtrl', ['ngStorage'])
.controller('SearchCtrl', function (
	$scope, 
	$log, 
	$http, 
	$ionicScrollDelegate, 
	$ionicLoading, 
	$ionicPopup, 
	ActionSheetSvc,
	$localStorage
	) {
        var vm = this;
        vm.searchString = '';
        vm.searchResults;
        vm.songGroups = {};
        $localStorage.$default({
        	recentSearches: []
        });
        vm.recentSearches = $localStorage.recentSearches;
        vm.noResults = false;
        vm.searchComplete = false;
        vm.search = search;
        vm.exactSearch = exactSearch;
        vm.hasSongGroups = hasSongGroups;
        vm.clearSearch = clearSearch;
        vm.selectSong = selectSong;
		vm.hasMoreData = hasMoreData;
		vm.nextPage = nextPage;
		vm.clearHistory = clearHistory;

        function exactSearch(searchString) {
            search('"' + searchString + '"');
        }
        
        function search(searchString) {
        	vm.searchString = searchString;
        	
        	if ((vm.searchResults || {}).query !== searchString) {
                if (!_.isEmpty(searchString)) { 
                	$ionicLoading.show({
                        delay: 500, 
                        noBackdrop: true
                    });

                	query({
                        query: searchString,
                        per_page: 50,
                        page: 1, 
                        by: 'title'
                    });
                }
				
                if (!_.isEmpty(vm.searchResults)) {
            		delete vm.searchResults;
        		}
        	}
        }
        
        function query(params) {

            $http.get('/api/v1/songs/query', {params: params}).then(function(response) {
                $scope.$broadcast('scroll.infiniteScrollComplete');
            	if (vm.searchResults === undefined) {
                    $ionicScrollDelegate.scrollTop();
                	vm.searchResults = response.data;
            	} else {
					Array.prototype.splice.apply(vm.searchResults.songs, [vm.searchResults.songs.length, 0].concat(response.data.songs));
            	}
            	angular.extend(vm.searchResults, params);
                vm.songGroups = _.groupBy(vm.searchResults.songs, 'artist');
                $ionicLoading.hide();
                if (hasSongGroups()) {
                	if (_.indexOf(vm.recentSearches, params.query)===-1) {
                		vm.recentSearches.unshift(params.query); 
                	}
                	vm.noResults = false;
                	vm.searchComplete = true;
                } else {
                	vm.noResults = true;
                }
            }, function(response) {
				$ionicPopup.alert({
					title: "Error",
					template: JSON.stringify(response.data)
				});
                $log.error(response);
                $ionicLoading.hide();
            });
        }
        
		function nextPage() {
			var params = {
				query: vm.searchResults.query, 
				page: vm.searchResults.page + 1, 
				per_page: vm.searchResults.per_page, 
				by: 'title'
			}
			query(params);
		}
		
        function selectSong(song) {
        	ActionSheetSvc.searchSongActions(song);
        }
               
        function clearSearch() {
        	vm.searchString = '';
        	vm.songGroups = {};
    		delete vm.searchResults;
        	vm.noResults = false;
        }
        
        function hasSongGroups() {
        	return _.keys(vm.songGroups).length > 0 ? true : false;
        }
        
		function hasMoreData() {
			return (vm.searchResults || {}).page < (vm.searchResults || {}).total_pages;
		}
		
		function clearHistory() {
			vm.recentSearches.splice(0, vm.recentSearches.length);
		}
    });
});
