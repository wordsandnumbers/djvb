define(['angular', 'controllers/main', 'controllers/about', 'controllers/search', 'controllers/login'] /*deps*/ , function (angular, MainCtrl, AboutCtrl, SearchCtrl, LoginCtrl) /*invoke*/ {
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
        .module('djvbApp', ['djvbApp.controllers.MainCtrl',
            'djvbApp.controllers.AboutCtrl',
            'djvbApp.controllers.SearchCtrl',
            'djvbApp.controllers.LoginCtrl',
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
        .config(function ($stateProvider, $urlRouterProvider) {
            $stateProvider
            .state('login', {
                url: '/login',
                templateUrl: 'login.html', 
                controller: 'LoginCtrl as vm'
            })
            .state('search', {
                url: '/search',
                templateUrl: 'search.html', 
                controller: 'SearchCtrl as vm'
            })
            .state('sing', {
                url: '/sing',
                templateUrl: 'sing.html'
            })
            .state('home', {
                url: '/home',
                templateUrl: 'home.html'
            });
            // if none of the above states are matched, use this as the fallback
            $urlRouterProvider.otherwise('login');
        });
});