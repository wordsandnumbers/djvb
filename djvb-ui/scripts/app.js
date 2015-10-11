/*jshint unused: vars */
define(['angular', 'controllers/about', 'controllers/search', 'controllers/login', 'controllers/home', 'services/user', 'controllers/usersetup', 'services/queue', 'controllers/queue', 'services/playlists', 'controllers/playlists', 'controllers/playlist']/*deps*/,
function (angular, AboutCtrl, SearchCtrl, LoginCtrl, HomeCtrl, UserService, UserSetupCtrl, QueueService, QueueCtrl, PlaylistsService, PlaylistsCtrl, PlaylistCtrl)/*invoke*/{
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
            .state('login', {
                url: '/login',
                templateUrl: 'views/login.html', 
                controller: 'LoginCtrl as vm'
            })
            .state('setup', {
                url: '/setup',
                templateUrl: 'views/setup.html', 
                controller: 'UserSetupCtrl as vm', 
                resolve: {
                	user: function(UserSvc){
                		return UserSvc.getUser();
                	}
                }
            })
            .state('search', {
                url: '/search',
                templateUrl: 'views/search.html', 
                controller: 'SearchCtrl as vm'
            })
            .state('sing', {
                url: '/sing',
                templateUrl: 'views/sing.html', 
                controller: 'QueueCtrl as vm'
            })
            .state('playlists', {
                url: '/playlists',
                templateUrl: 'views/playlists.html', 
                controller: 'PlaylistsCtrl as vm'
            })
            .state('playlist', {
                url: '/playlists/:playlistId',
                templateUrl: 'views/playlist.html', 
                controller: 'PlaylistCtrl as vm'
            })
            .state('home', {
                url: '/home',
                templateUrl: 'views/home.html', 
                controller: 'HomeCtrl as vm'
            });
            // if none of the above states are matched, use this as the fallback
            $urlRouterProvider.otherwise('login');
            
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