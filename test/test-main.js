var tests = [];
for (var file in window.__karma__.files) {
  if (window.__karma__.files.hasOwnProperty(file)) {
    // Removed "Spec" naming from files
    if (/Spec\.js$/.test(file)) {
      tests.push(file);
    }
  }
}

requirejs.config({
    // Karma serves files from '/base'
    baseUrl: '/base/app/scripts',

    paths: {
		angular: '../../bower_components/angular/angular',
		'angular-animate': '../../bower_components/angular-animate/angular-animate',
		'angular-sanitize': '../../bower_components/angular-sanitize/angular-sanitize',
		ionic: '../../bower_components/ionic/release/js/ionic',
		'ionic-angular': '../../bower_components/ionic/release/js/ionic-angular',
		'angular-ui-router': '../../bower_components/angular-ui-router/release/angular-ui-router',
		lodash: '../../bower_components/lodash/lodash',
		'angular-aria': '../../bower_components/angular-aria/angular-aria',
		'angular-mocks': '../../bower_components/angular-mocks/angular-mocks',
		'ng-file-upload': '../../bower_components/ng-file-upload/ng-file-upload',
		'ng-img-crop': '../../bower_components/ng-img-crop/compile/minified/ng-img-crop',
		ngstorage: '../../bower_components/ngstorage/ngStorage',
		firebase: 'https://www.gstatic.com/firebasejs/4.5.0/firebase',
		firebaseui: '../../bower_components/firebaseui/dist/firebaseui',
		'dialog-polyfill': '../../bower_components/dialog-polyfill/dialog-polyfill'
	},

    shim: {
        'angular' : {'exports' : 'angular'},
        'angular-route': ['angular'],
        'angular-cookies': ['angular'],
        'angular-sanitize': ['angular'],
        'angular-resource': ['angular'],
        'angular-animate': ['angular'],
        'angular-touch': ['angular'],
        'angular-mocks': {
          deps:['angular'],
          'exports':'angular.mock'
        }
    },

    // ask Require.js to load these files (all our tests)
    deps: tests,

    // start test run, once Require.js is done
    callback: window.__karma__.start
});
