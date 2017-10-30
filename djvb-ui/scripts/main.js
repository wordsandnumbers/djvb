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
		'ng-file-upload': '../../bower_components/ng-file-upload/ng-file-upload',
		'ng-img-crop': '../../bower_components/ng-img-crop/compile/minified/ng-img-crop',
		ngstorage: '../../bower_components/ngstorage/ngStorage',
		firebase: 'https://www.gstatic.com/firebasejs/4.5.0/firebase',
		firebaseui: '../../bower_components/firebaseui/dist/firebaseui'
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
		firebase: {
			exports: 'firebase'
		},
		firebaseui: {
			exports: 'firebaseui'
		},
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
		'ng-img-crop': [
			'angular'
		],
		'ng-file-upload': [
			'angular'
		],
		ngstorage: [
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
	'firebase',
	'angular-sanitize',
	'angular-animate',
	'ionic',
	'ionic-angular',
	'angular-ui-router',
	'lodash',
	'digits',
	'ngstorage',
	'ng-file-upload',
	'ng-img-crop'
], function (angular, app, firebase, ngSanitize, ngAnimate, ionic, ngIonic, ngRouter, _, Digits, ngStorage) {
	'use strict';
	/* jshint ignore:start */

	// Initialize Firebase
    var config = {
        apiKey: "AIzaSyDmzYk2d5eotsPbRgiH_hyB0QPxu4IOTy4",
        authDomain: "djvb-878ca.firebaseapp.com",
        databaseURL: "https://djvb-878ca.firebaseio.com",
        projectId: "djvb-878ca",
        storageBucket: "djvb-878ca.appspot.com",
        messagingSenderId: "43769785731"
    };
	firebase.initializeApp(config);

	var $html = angular.element(document.getElementsByTagName('html')[0]);
	/* jshint ignore:end */
	angular.element().ready(function () {
		angular.resumeBootstrap([app.name]);
	});
});
