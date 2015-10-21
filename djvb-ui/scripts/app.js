/*jshint unused: vars */
define(['angular', 'controllers/about', 'controllers/search', 'controllers/login', 'controllers/home', 'services/user', 'controllers/usersetup', 'services/queue', 'controllers/queue', 'services/playlists', 'controllers/playlists', 'controllers/playlist', 'controllers/playhistory', 'controllers/favorites', 'services/actionsheet']/*deps*/,
function (angular, AboutCtrl, SearchCtrl, LoginCtrl, HomeCtrl, UserService, UserSetupCtrl, QueueService, QueueCtrl, PlaylistsService, PlaylistsCtrl, PlaylistCtrl, PlayHistoryCtrl, FavoritesCtrl, ActionSheetService)/*invoke*/{
    'use strict';

    /**
     * @ngdoc overview
     * @name djvbApp
     * @description
     * # djvbApp
     *
     * Main module of the application.
     */
    return angular
        .module('djvbApp', [
            'djvbApp.controllers.AboutCtrl',
            'djvbApp.controllers.SearchCtrl',
            'djvbApp.controllers.LoginCtrl',
			'djvbApp.services.UserSvc',
			'djvbApp.controllers.HomeCtrl',
			'djvbApp.controllers.UserSetupCtrl',
			'djvbApp.services.QueueSvc',
			'djvbApp.controllers.QueueCtrl',
			'djvbApp.services.PlaylistsSvc',
			'djvbApp.controllers.PlaylistsCtrl',
			'djvbApp.controllers.PlaylistCtrl',
			'djvbApp.controllers.PlayHistoryCtrl',
			'djvbApp.controllers.FavoritesCtrl',
			'djvbApp.services.ActionSheetSvc',
/*angJSDeps*/
            'ngCookies',
            'ngResource',
            'ngSanitize',
            'ngRoute',
            'ngAnimate',
            'ngTouch',
            'ionic',
            'ui.router'
        ])
        .run(function ($ionicPlatform) {
            $ionicPlatform.ready(function () {
                // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
                // for form inputs)
                if (window.cordova && window.cordova.plugins.Keyboard) {
                    cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
                }
                if (window.StatusBar) {
                    // org.apache.cordova.statusbar required
                    StatusBar.styleDefault();
                }
            });
        })
        .config(function ($stateProvider, $urlRouterProvider, $httpProvider) {
            $stateProvider
			.state('tabs', {
				url: "",
				abstract: true,
				templateUrl: "views/tabs.html"
			})
            .state('login', {
                url: '/login',
                templateUrl: 'views/login.html', 
                controller: 'LoginCtrl as vm'
            })
            .state('tabs.setup', {
                url: '/setup',
                views: {
                	'tab-home': {
                        templateUrl: 'views/setup.html', 
                        controller: 'UserSetupCtrl as vm', 
                        resolve: {
                        	user: function(UserSvc){
                        		return UserSvc.getUser();
                        	}
                        }
                	}
                }
            })
            .state('tabs.home', {
                url: '/home',
                views: {
                	'tab-home': {
                        templateUrl: 'views/home.html', 
                        controller: 'HomeCtrl as vm'                		
                	}
                }
            })
            .state('tabs.playlists', {
                url: '/playlists',
                views: {
                	'tab-home': {
                        templateUrl: 'views/playlists.html', 
                        controller: 'PlaylistsCtrl as vm'
                	}
                }
            })
            .state('tabs.playlist', {
                url: '/playlists/:playlistId',
                views: {
                	'tab-home': {
                        templateUrl: 'views/playlist.html', 
                        controller: 'PlaylistCtrl as vm'
                	}
                }
            })
            .state('tabs.playHistory', {
                url: '/playhistory',
                views: {
                	'tab-home': {
                        templateUrl: 'views/playhistory.html', 
                        controller: 'PlayHistoryCtrl as vm'
                	}
                }
            })
            .state('tabs.favorites', {
                url: '/favorites',
                views: {
                	'tab-home': {
                        templateUrl: 'views/favorites.html', 
                        controller: 'FavoritesCtrl as vm'
                	}
                }
            })
            .state('tabs.search', {
                url: '/search',
                views: {
                	'tab-search': {
                        templateUrl: 'views/search.html', 
                        controller: 'SearchCtrl as vm'
                	}
                }
            })
            .state('tabs.sing', {
                url: '/sing',
                views: {
                	'tab-sing': {
                        templateUrl: 'views/sing.html', 
                        controller: 'QueueCtrl as vm'
                	}
                }
            });
            
            // if none of the above states are matched, use this as the fallback
            $urlRouterProvider.otherwise('/home');
            
    		// Globablly intercept response, redirect to login if not authorized for API
    		$httpProvider.interceptors.push(function($q, $location) {
    			return {
    				responseError: function(response) {
    					if (response.status === 401) {
    						$location.url('/login')
    					}
    					return $q.reject(response);
    				}
    			};
    		});
        });
});