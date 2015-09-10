(function() {
	'use strict';
	
	angular
		.module('ngTruncateNumber', [])
		.filter('truncatenum', function() {
		  return function(input) {
		    return input | 0;
		  };
		});
})();
