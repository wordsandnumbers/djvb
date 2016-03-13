define(['angular', 'lodash'], function (angular, _) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.controller:BrowseCtrl
   * @description
   * # BrowseCtrl
   * Controller of the djvbApp
   */
  angular.module('djvbApp.controllers.BrowseCtrl', [])
.controller('BrowseCtrl', function (
	$scope, 
	$log, 
	$http, 
	$ionicScrollDelegate, 
	$ionicLoading, 
	$ionicPopup, 
	ActionSheetSvc,
	$stateParams
	) {
        var vm = this;
        var titles = {
    		popularity: 'Top Songs',
    		recently_added: 'Recently Added'
        }
        
        vm.searchString = '';
        vm.searchResults;
        vm.songGroups = {};
        vm.noResults = false;
        vm.searchComplete = false;
        vm.browse = browse;
        vm.selectSong = selectSong;
		vm.hasMoreData = hasMoreData;
		vm.nextPage = nextPage;
		vm.title = titles[$stateParams.by];
		browse();
		
/*		ARTIST("artist"),
		TITLE("title"),
		POPULAR("popularity"),
		RECENT("recently_added");*/
      
        function browse() {        	
        	$ionicLoading.show({
                delay: 500, 
                noBackdrop: true
            });

        	query({
                per_page: 50,
                page: 1, 
                by: $stateParams.by
            });
        }
        
        function query(params) {
            $http.get('/api/v1/songs/browse', {params: params}).then(function(response) {
                $scope.$broadcast('scroll.infiniteScrollComplete');
            	if (vm.searchResults === undefined) {
                    $ionicScrollDelegate.scrollTop();
                	vm.searchResults = response.data;
            	} else {
					Array.prototype.splice.apply(vm.searchResults.songs, [vm.searchResults.songs.length, 0].concat(response.data.songs));
            	}
                vm.songGroups = _.groupBy(vm.searchResults.songs, function (song) {
                	return song.added_on;
                });
                angular.extend(vm.searchResults, params);
                $ionicLoading.hide();
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
				by: $stateParams.by
			}
			
			query(params);
		}
		
        function selectSong(song) {
        	ActionSheetSvc.searchSongActions(song);
        }
        
		function hasMoreData() {
			return (vm.searchResults || {}).page < (vm.searchResults || {}).total_pages;
		}
    });
});
