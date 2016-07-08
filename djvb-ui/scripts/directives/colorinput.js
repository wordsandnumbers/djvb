define(['angular'], function (angular) {
	'use strict';

	/**
	 * @ngdoc function
	 * @name djvbApp.directives:Capitalize
	 * @description
	 * # Capitalize
	 * Directive of the djvbApp
	 */
	angular.module('djvbApp.directives.ColorInput', [])
		.directive('colorInput', function ($ionicPopover, constants) {
			return {
				link: ColorInputLink,
				require: 'ngModel',
				restrict: 'E',
				scope: {
					color: '=ngModel'
				},
				template: '<button class="button button-small button-light" title="Selected color: {{color}}. Change user color" ng-style="{\'background-color\': color}" ng-click="pickColor($event)">Change</button>'
			};
			
			function ColorInputLink (scope, element, attrs, modelCtrl) {

				scope.colorOptions = ['#72C2FF', '#6798E6', '#CEA1E1', '#FFADED', '#FC809B', '#FFC787', '#FFF074', '#A6FC81', '#09E6AE', '#18C4C7',
				'#247ba0', '#70c1b3', '#b2dbbf', '#f3ffbd', '#ff1654',
				'#eae8ff', '#d8d5db', '#adacb5', '#b0d7ff', '#8e443d', '#cb9173'];

				$ionicPopover.fromTemplateUrl(constants.resourcesBaseUrl + '/views/colorpickerpopover.html', {
					scope: scope
				}).then(function(popover) {
					scope.popover = popover;
				});

				scope.setColor = function (color) {
					modelCtrl.$setViewValue(color);
					scope.popover.hide();
				};

				scope.pickColor = function ($event) {
					scope.popover.show($event);
				};
				
				scope.isSelected = function (color) {
					return color.toLowerCase() === scope.color.toLowerCase();
				}
			}
		});
});
