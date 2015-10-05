(function () {
  'use strict';

  angular
    .module('webApp')
    .service('UtilsService', UtilsService);


  function UtilsService() {
    var vm = this;
    vm.findElementInJSONArray = findElementInJSONArray;
    vm.removeItemsFromArray = removeItemsFromArray;

    function findElementInJSONArray(array, element, attr) {
      var found = false;
      var position = -1;
      if (array && element && attr) {
        var i = 0;
        while (!found && i < array.length) {
          var currentElement = array[i];
          if (currentElement[attr] === element[attr]) {
            found = true;
            position = i;
          } else {
            ++i;
          }
        }
      }
      return position;
    }

    function removeItemsFromArray(array, positions) {
      var position = null;
      var arrayResult = array;
      if (array && positions) {
        positions = positions.sort();
        var removedElements = 0;
        for (var i = 0; i < positions.length; ++i) {
          position = positions[i] - removedElements;
          arrayResult.splice(position, 1);
          removedElements++;
        }
      }
      return arrayResult;
    }
  }
})();
