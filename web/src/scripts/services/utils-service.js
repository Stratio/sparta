(function () {
  'use strict';

  angular
    .module('webApp')
    .service('UtilsService', UtilsService);

  UtilsService.$inject = ['$filter'];

  function UtilsService($filter) {
    var vm = this;
    vm.findElementInJSONArray = findElementInJSONArray;
    vm.removeItemsFromArray = removeItemsFromArray;
    vm.autoIncrementName = autoIncrementName;
    vm.getNamesJSONArray = getNamesJSONArray;
    vm.getItemNames = getItemNames;
    vm.addFragmentCount = addFragmentCount;
    vm.subtractFragmentCount = subtractFragmentCount;

    function findElementInJSONArray(array, element, attr) {
      var found = false;
      var position = -1;
      if (array && array.length > 0 && element && attr) {
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

    function autoIncrementName(input) {
      var output = "";
      var actual = 2;
      var pattern = input.match(/\(\d+\)$/);

      if (pattern) {
        output = input.replace(pattern, "");
        actual = parseInt(pattern[0].substring(1, pattern[0].length - 1)) + 1;
      } else {
        output = input;
      }
      output = output + '(' + actual + ')';

      return output;
    }

    //function getItemNames(itemList) {
    function getNamesJSONArray(itemList) {
      var itemNames = [];
      if (itemList) {
        for (var i = 0; i < itemList.length; i++) {
          if (itemList[i].name) {
            var lowerCaseName = itemList[i].name.toLowerCase();
            var fragment = {'name': lowerCaseName};
            itemNames.push(fragment);
          }
        }
      }
      return itemNames;
    }

    //getPolicyNames
    function getItemNames(array) {
      var names = [];
      if (array) {
        for (var i = 0; i < array.length; i++) {
          if (array[i].name)
            names.push(array[i].name);
        }
      }

      return names;
    }

    function addFragmentCount(inputTypeList, inputType) {
      var newInputCount = $filter('filter')(inputTypeList, {'type':inputType}, true)[0];
      if (!newInputCount) {
        var newInpuntCount = {'type': inputType, 'count': 1};
        inputTypeList.push(newInpuntCount);
      }
      else {
        newInputCount.count++;
      }
    };

    function subtractFragmentCount(inputTypeList, inputType, filter) {
      var newInputCount = $filter('filter')(inputTypeList, {'type':inputType}, true)[0];
      newInputCount.count--;
      if (newInputCount.count === 0) {
        for (var i=0; i < inputTypeList.length; i++) {
          if (inputTypeList[i].type === inputType) {
            inputTypeList.splice(i,1);
            filter.element.type = "";
            filter.name = "";
          }
        }
      }
    };
  }
})
();
