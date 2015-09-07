(function() {
	'use strict';
	
	angular
		.module('ngTruncate', [])
		.filter('truncatenum', function() {
		  return function(input) {
		    return input | 0;
		  };
		});
})();