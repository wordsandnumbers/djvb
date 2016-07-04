define(['angular', 'lodash'], function (angular, _) {
  'use strict';

  /**
   * @ngdoc service
   * @name djvbApp.TagsSvc
   * @description
   * # Tags
   * Service in the djvbApp.
   */
  angular.module('djvbApp.services.TagsSvc', [])
	.service('TagsSvc', function ($http, $q, constants) {
		
		var tags = [],
			tagsPromise;

		return {
			getTags: getTags
		};
		
		function getTags() {
			if (tagsPromise === undefined || _.get(tagsPromise, '$$state.status') === 2) {
				tagsPromise = $q(function (resolve, reject) {
					$http.get('/api/v1/songs/tags').then(function(response) {
						angular.copy(response.data, tags);
						resolve(tags);
					}, function(errorResponse) {
						reject(errorResponse);
					});
				});
			}
			return tagsPromise;
		}
	});
});
