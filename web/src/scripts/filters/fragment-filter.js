(function() {
  'use strict';

  angular
    .module('webApp')
    .filter('fragmentFilter', function() {
      return function(fragments, filterType) {
		if (filterType && filterType.element.type !== '') {
			var output = [];
        	for (var i=0; i < fragments.length; i++) {
        		if (fragments[i].element.type === filterType.element.type) {
        			output.push(fragments[i]);
        		}
        	}
			return output;
		}
		else {
			return fragments;
		}
      };
    });
})();
