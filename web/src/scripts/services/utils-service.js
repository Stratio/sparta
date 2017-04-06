/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function () {
  'use strict';

  angular
    .module('webApp')
    .service('UtilsService', UtilsService);

  UtilsService.$inject = ['$filter'];

  function UtilsService($filter) {
    var vm = this;
    vm.findElementInJSONArray = findElementInJSONArray;
    vm.filterByAttribute = filterByAttribute;
    vm.removeItemsFromArray = removeItemsFromArray;
    vm.autoIncrementName = autoIncrementName;
    vm.getNamesJSONArray = getNamesJSONArray;
    vm.getItemNames = getItemNames;
    vm.addFragmentCount = addFragmentCount;
    vm.subtractFragmentCount = subtractFragmentCount;
    vm.getInCamelCase = getInCamelCase;
    vm.convertDottedPropertiesToJson = convertDottedPropertiesToJson;
    vm.getFilteredJSONByArray = getFilteredJSONByArray;
    vm.getFilteredArray = getFilteredArray;
    vm.removeDuplicatedJSONs = removeDuplicatedJSONs;
    vm.generateOptionListFromStringArray = generateOptionListFromStringArray;
    vm.camelToDash = camelToDash;
    vm.getArrayElementPosition = getArrayElementPosition;

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

    function filterByAttribute(array, attr, value) {
      var filter = {};
      filter[attr] = value;

      return $filter('filter')(array, filter, true);
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
            var fragment = { 'name': lowerCaseName };
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
      var newInputCount = $filter('filter')(inputTypeList, { 'type': inputType }, true)[0];
      if (!newInputCount) {
        var newInpuntCount = { 'type': inputType, 'count': 1 };
        inputTypeList.push(newInpuntCount);
      }
      else {
        newInputCount.count++;
      }
    }

    function subtractFragmentCount(inputTypeList, inputType) {
      var newInputCount = $filter('filter')(inputTypeList, { 'type': inputType }, true)[0];
      newInputCount.count--;
      if (newInputCount.count === 0) {
        for (var i = 0; i < inputTypeList.length; i++) {
          if (inputTypeList[i].type === inputType) {
            inputTypeList.splice(i, 1);
          }
        }
      }
    }

    function getInCamelCase(string, separator, firstUpperCase) {
      if (string && separator) {
        var words = string.split(separator);

        var result = words[0].toLowerCase();
        if (firstUpperCase) {
          result = result[0].toUpperCase() + result.substring(1);
        }
        for (var i = 1; i < words.length; ++i) {
          var word = words[i].toLowerCase();
          result = result + word[0].toUpperCase() + word.substring(1);
        }

        return result;
      }
      return string;
    }

    function convertDottedPropertiesToJson(object) {
      for (var key in object) {
        var splittedKey = key.split(".");
        if (splittedKey.length > 1) {
          var tempProperty = "";
          tempProperty = object;
          if (!object[splittedKey[0]]) {
            object[splittedKey[0]] = {};
          }
          for (var i = 0; i < splittedKey.length - 1; ++i) {
            tempProperty = tempProperty[splittedKey[i]];
          }
          if (!tempProperty[splittedKey[splittedKey.length - 1]]) {
            tempProperty[splittedKey[splittedKey.length - 1]] = {};
          }
          tempProperty[splittedKey[splittedKey.length - 1]] = object[key];
          delete object[key];
        }
      }
      return object;
    }
    //allOutputs, cubeOutputs
    function getFilteredJSONByArray(JsonArray, array, attribute) {
      var filteredElements = [];
      if (array) {
        for (var i = 0; i < array.length; ++i) {
          var filter = {};
          filter[attribute] = array[i];
          var filteredOutput = $filter('filter')(JsonArray, filter, true)[0];
          if (filteredOutput) {
            filteredElements.push(filteredOutput);
          }
        }
      }
      return filteredElements;
    }

    function getFilteredArray(arr1, array, attribute) {
      var arr3 = [];
      var arr2 = array.filter(function(item, pos) {
          return array.indexOf(item) == pos;
      })

      for (var i = 0; i < arr1.length; ++i) {
        var aux = arr1[i][attribute];
        if(array.indexOf(aux)!=-1){
          arr3.push(arr1[i]);
        }
      }
      return arr3;
    }

    function removeDuplicatedJSONs(array, attribute) {
      var resultArray = [];
      if (array) {
        for (var i = 0; i < array.length; ++i) {
          var filter = {};
          filter[attribute] = array[i][attribute];
          var foundElement = $filter('filter')(resultArray, filter, true)[0];
          if (!foundElement) {
            resultArray.push(array[i]);
          }
        }
      }
      return resultArray;
    }

    function generateOptionListFromStringArray(array) {
      var optionList = [];
      if (array) {
        for (var i = 0; i < array.length; ++i) {
          optionList.push({ "label": array[i], "value": array[i] });
        }
      }
      return optionList;
    }

    function camelToDash(word, dash) {
      if (!dash) {
        dash = '-';
      }
      return word.replace(/([A-Z])/g, function ($1) { return dash + $1.toLowerCase(); });
    }

    function getArrayElementPosition(array, atribute, value){
      for(var i=0; i<array.length; i++){
        if(array[i][atribute] === value){
          return i;
        }
      };
    }
  }
})
  ();
