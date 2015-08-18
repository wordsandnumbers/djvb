/*jshint unused: vars */
require.config({
  paths: {
    angular: '../../bower_components/angular/angular',
    'angular-animate': '../../bower_components/angular-animate/angular-animate',
    'angular-cookies': '../../bower_components/angular-cookies/angular-cookies',
    'angular-mocks': '../../bower_components/angular-mocks/angular-mocks',
    'angular-resource': '../../bower_components/angular-resource/angular-resource',
    'angular-route': '../../bower_components/angular-route/angular-route',
    'angular-sanitize': '../../bower_components/angular-sanitize/angular-sanitize',
    'angular-touch': '../../bower_components/angular-touch/angular-touch',
    'angular-aria': '../../bower_components/angular-aria/angular-aria',
    'angular-material': '../../bower_components/angular-material/angular-material',
    ionic: '../../bower_components/ionic/release/js/ionic',
    'ionic-angular': '../../bower_components/ionic/release/js/ionic-angular',
    'angular-ui-router': '../../bower_components/angular-ui-router/release/angular-ui-router',
    lodash: '../../bower_components/lodash/lodash',
    digits: 'https://cdn.digits.com/1/sdk'
  },
  shim: {
    angular: {
      exports: 'angular'
    },
    'angular-route': [
      'angular'
    ],
    'angular-cookies': [
      'angular'
    ],
    'angular-sanitize': [
      'angular'
    ],
    'angular-resource': [
      'angular'
    ],
    'angular-animate': [
      'angular'
    ],
    'angular-touch': [
      'angular'
    ],
    'angular-mocks': {
      deps: [
        'angular'
      ],
      exports: 'angular.mock'
    },
    'angular-aria': [
      'angular'
    ],
    ionic: [
      'angular'
    ],
    'ionic-angular': [
      'angular',
      'ionic'
    ],
    'angular-ui-router': [
      'angular'
    ]
  },
  priority: [
    'angular'
  ],
  packages: [

  ]
});

//http://code.angularjs.org/1.2.1/docs/guide/bootstrap#overview_deferred-bootstrap
window.name = 'NG_DEFER_BOOTSTRAP!';

require([
  'angular',
  'app',
  'angular-route',
  'angular-cookies',
  'angular-sanitize',
  'angular-resource',
  'angular-animate',
  'angular-touch', 
  'angular-aria', 
  'ionic', 
  'ionic-angular', 
  'angular-ui-router', 
  'lodash', 
  'digits'
], function(angular, app, ngRoutes, ngCookies, ngSanitize, ngResource, ngAnimate, ngTouch, ngAria, ionic, ngIonic, ngRouter, _, Digits) {
  'use strict';
  /* jshint ignore:start */
    
  Digits.init({ consumerKey: 'Z0aFJy5A3kpsgcazoffu2sP1f' });
  var $html = angular.element(document.getElementsByTagName('html')[0]);
  /* jshint ignore:end */
  angular.element().ready(function() {
    angular.resumeBootstrap([app.name]);
  });
});
