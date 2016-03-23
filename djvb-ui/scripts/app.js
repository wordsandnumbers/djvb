/*jshint unused: vars */
define(['angular', 'controllers/browse', 'controllers/search', 'controllers/tabs', 'controllers/login', 'controllers/home', 'services/user', 'controllers/usersetup', 'services/queue', 'controllers/queue', 'services/playlists', 'controllers/playlists', 'controllers/playlist', 'controllers/playhistory', 'controllers/favorites', 'controllers/digitscallback', 'services/actionsheet', 'directives/focusme', 'directives/capitalize', 'templates']/*deps*/,
function (angular)/*invoke*/{
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
            'djvbApp.controllers.BrowseCtrl',
			'djvbApp.controllers.DigitsCallbackCtrl',
			'djvbApp.controllers.FavoritesCtrl',
			'djvbApp.controllers.HomeCtrl',
            'djvbApp.controllers.LoginCtrl',
			'djvbApp.controllers.PlaylistsCtrl',
			'djvbApp.controllers.PlaylistCtrl',
			'djvbApp.controllers.PlayHistoryCtrl',
			'djvbApp.controllers.QueueCtrl',
            'djvbApp.controllers.SearchCtrl',
            'djvbApp.controllers.TabsCtrl',
			'djvbApp.controllers.UserSetupCtrl',
			'djvbApp.directives.Capitalize', 
			'djvbApp.directives.FocusMe',
			'djvbApp.services.ActionSheetSvc',
			'djvbApp.services.PlaylistsSvc',
			'djvbApp.services.QueueSvc',
			'djvbApp.services.UserSvc',
			'djvbApp.templates',
			/*angJSDeps*/
            'ionic',
            'ngSanitize',
            'ngAnimate',
            'ui.router'
        ])
        .run(function ($rootScope, $ionicPlatform, $location, UserSvc, $state) {

            $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
            	UserSvc.getUser().then(function() {
                    // Don't allow login route if authenticated.
                	if (toState.url === '/login' && $rootScope.authenticated === true) {
                        $state.go('tabs.home');
                    // Route to login if not authenticated.
                	} else if (toState.url !== '/login' && $rootScope.authenticated === false) {
                		$state.go('login', fromParams);
                	}
            	})
            });
            
        	// Get user immediately on app load.
        	UserSvc.getUser();
        	
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
                
                angular.element(document.getElementById('apploading')).remove();
            });
        })
        .config(function ($stateProvider, $urlRouterProvider, $httpProvider) {
    		// Globablly intercept response, redirect to login if not authorized for API
    		$httpProvider.interceptors.push(function($q, $location, $rootScope, $injector, $stateParams) {
    			return {
    				responseError: function(response) {
    					if (response.status === 401) {
    						$rootScope.authenticated = false;
    						var $state = $injector.get('$state');
    						$state.go('login', $state.params);
    					}
    					return $q.reject(response);
    				}
    			};
    		});

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
            .state('digitscallback', {
                url: '/digitscallback?X-Verify-Credentials-Authorization&X-Auth-Service-Provider',
                controller: 'DigitsCallbackCtrl as vm'
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
                	'tab-songs': {
                        templateUrl: 'views/search.html', 
                        controller: 'SearchCtrl as vm'
                	}
                }
            })
            .state('tabs.browse', {
                url: '/browse/{mode}',
                views: {
                	'tab-songs': {
                        templateUrl: 'views/browse.html', 
                        controller: 'BrowseCtrl as vm'
                	}
                }
            })
            .state('tabs.browsequery', {
                url: '/{mode}/{query}',
                views: {
                	'tab-songs': {
                        templateUrl: 'views/browse.html', 
                        controller: 'BrowseCtrl as vm'
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
        });
});