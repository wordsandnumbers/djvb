define(['angular'], function (angular) {
  'use strict';

  /**
   * @ngdoc function
   * @name djvbApp.directives:FocusMe
   * @description
   * # FocusMe
   * Directive of the djvbApp
   */
  angular.module('djvbApp.directives.FocusMe', [])
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
  });
});
