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
    		recently_added: 'New Songs',
    		tag: $stateParams.query,
    		artist: $stateParams.query
        }
        
        vm.mode = $stateParams.mode;
        vm.searchResults;
        vm.songGroups = {};
        vm.noResults = false;
        vm.searchComplete = false;
        vm.browse = browse;
        vm.selectSong = selectSong;
		vm.hasMoreData = hasMoreData;
		vm.nextPage = nextPage;
		vm.title = titles[$stateParams.mode];
		var queries = {
			popularity: {
	            per_page: 50,
	            by: $stateParams.mode
	        },
			recently_added: {
	            per_page: 50,
	            by: $stateParams.mode
	        },
			tag: {
				tag: $stateParams.query,
	            per_page: 50,
	            by: 'popularity'
	        },
	        artist: {
	        	query: '"' + $stateParams.query + '"',
	        	per_page: 50,
	        	by: 'title'
	        }
		};
	      
		browse();
		
        function browse() {        	
        	$ionicLoading.show({
                noBackdrop: true
            });

        	query(queries[$stateParams.mode]);
        }
        
        function query(params) {
            $http.get('/api/v1/songs/' + ($stateParams.mode === 'artist' ? 'query' : 'browse'), {params: params}).then(function(response) {
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
			}
			
			angular.extend(params, queries[$stateParams.mode]);
			
			query(params);
		}
		
        function selectSong(song) {
        	ActionSheetSvc.browseSongActions(song);
        }
        
		function hasMoreData() {
			return (vm.searchResults || {}).page < (vm.searchResults || {}).total_pages;
		}
    });
});
