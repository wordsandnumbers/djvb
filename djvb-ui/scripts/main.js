/*jshint unused: vars */
require.config({
  paths: {
    angular: '../../bower_components/angular/angular',
    'angular-animate': '../../bower_components/angular-animate/angular-animate',
    'angular-sanitize': '../../bower_components/angular-sanitize/angular-sanitize',
    ionic: '../../bower_components/ionic/release/js/ionic',
    'ionic-angular': '../../bower_components/ionic/release/js/ionic-angular',
    'angular-ui-router': '../../bower_components/angular-ui-router/release/angular-ui-router',
    lodash: '../../bower_components/lodash/lodash',
    digits: 'https://cdn.digits.com/1/sdk',
    'angular-aria': '../../bower_components/angular-aria/angular-aria',
    'angular-mocks': '../../bower_components/angular-mocks/angular-mocks',
    ngstorage: '../../bower_components/ngstorage/ngStorage',
    'sockjs-client': '../../bower_components/sockjs-client/dist/sockjs',
    'stomp-websocket': '../../bower_components/stomp-websocket/lib/stomp.min',
    AngularStompDK: '../../bower_components/AngularStompDK/dist/angular-stomp'
  },
  shim: {
    angular: {
      exports: 'angular'
    },
    'angular-sanitize': [
      'angular'
    ],
    'angular-resource': [
      'angular'
    ],
    'angular-animate': [
      'angular'
    ],
    'AngularStompDK': [
      'angular',
      'stomp-websocket'
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
    ],
    ngstorage: [
      'angular'
    ],
    'sockjs-client': {
      exports: 'SockJS'
    },
    'stomp-websocket': {
      exports: 'Stomp'
    }
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
  'angular-sanitize',
  'angular-animate',
  'ionic', 
  'ionic-angular', 
  'angular-ui-router', 
  'lodash', 
  'digits',
  'ngstorage'
], function(angular, app, ngSanitize, ngAnimate, ionic, ngIonic, ngRouter, _, Digits, ngStorage) {
  'use strict';
  /* jshint ignore:start */
    
  Digits.init({ consumerKey: 'Z0aFJy5A3kpsgcazoffu2sP1f' });
  var $html = angular.element(document.getElementsByTagName('html')[0]);
  /* jshint ignore:end */
  angular.element().ready(function() {
    angular.resumeBootstrap([app.name]);
  });
});
