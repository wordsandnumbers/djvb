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
  .directive('focusMe', function($timeout) {
	  return {
	    scope: { trigger: '=focusMe' },
	    link: function(scope, element) {
	      scope.$watch('trigger', function(value) {
	        if(value === true) { 
	          //console.log('trigger',value);
	          $timeout(function() {
	            element[0].focus();
	            scope.trigger = false;
	          });
	        }
	      });
	    }
	  };
  })
.directive('capitalize', function() {
   return {
     require: 'ngModel',
     link: function(scope, element, attrs, modelCtrl) {
        var capitalize = function(inputValue) {
           if(inputValue == undefined) inputValue = '';
           var capitalized = inputValue.toUpperCase();
           if(capitalized !== inputValue) {
              modelCtrl.$setViewValue(capitalized);
              modelCtrl.$render();
            }         
            return capitalized;
         }
         modelCtrl.$parsers.push(capitalize);
         capitalize(scope[attrs.ngModel]);  // capitalize initial value
     }
   };
})
.controller('SearchCtrl', function ($scope, $log, $http, $ionicScrollDelegate, $ionicLoading, $ionicActionSheet, $ionicModal, $ionicPopup, QueueSvc, PlaylistsSvc, ActionSheetSvc) {
        var vm = this;
        vm.searchString = '';
        vm.songGroups = {};
        vm.recentSearches = [];
        vm.noResults = false;
        vm.searchComplete = false;
        vm.queues = [];
        vm.playlists;
        vm.search = search;
        vm.exactSearch = exactSearch;
        vm.hasSongGroups = hasSongGroups;
        vm.clearSearch = clearSearch;
        vm.selectSong = selectSong;
        
        QueueSvc.getQueues().then(function(queues) {
        	vm.queues = queues;
        })

        PlaylistsSvc.getPlaylistsList().then(function(playlists) {
        	vm.playlists = playlists;
        })

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
                    	vm.searchComplete = true;
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
        	ActionSheetSvc.searchSongActions(song);
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
