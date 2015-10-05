(function() {
  'use strict';

  angular
    .module('webApp')
    .filter('truncatenum', function() {
      return function(input) {
        return input | 0;
      };
    });
})();
