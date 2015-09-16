(function () {
  'use strict';

  angular
    .module('webApp')
    .service('UtilsService', UtilsService);


  function UtilsService() {
    var vm = this;
    vm.findElementInJSONArray = findElementInJSONArray;
    vm.findValueInJSONArray = findValueInJSONArray;

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

    function findValueInJSONArray(array, value, attr) {
      var found = false;
      var i = 0;
      var position = -1;
      while (!found && i < array.length) {
        var currentElement = array[i];
        if (currentElement[attr] === value) {
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
