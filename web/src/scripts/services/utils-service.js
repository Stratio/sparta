(function () {
  'use strict';

  angular
    .module('webApp')
    .service('UtilsService', UtilsService);


  function UtilsService() {
    var vm = this;
    vm.findElementInJSONArray = findElementInJSONArray;


    function findElementInJSONArray(array, element, attr) {
      var found = false;
      var i = 0;
      var position = -1;
      while (!found && i < array.length) {
        var currentElement = array[i];
        if (currentElement[attr] === element[attr]) {
          found = true;
          position = i;
        } else {
          ++i;
        }
      }
      return position;
    }
  }
})();
