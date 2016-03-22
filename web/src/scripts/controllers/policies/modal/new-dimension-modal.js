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

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewDimensionModalCtrl', NewDimensionModalCtrl);

  NewDimensionModalCtrl.$inject = ['$modalInstance', 'dimensionName', 'fieldName', 'dimensions',
    '$filter', 'UtilsService', 'template', 'isTimeDimension'];

  function NewDimensionModalCtrl($modalInstance, dimensionName, fieldName, dimensions, $filter, UtilsService, template, isTimeDimension) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.getPrecisionsOfType = getPrecisionsOfType;
    vm.setPropertyDisabled = setPropertyDisabled;
    vm.setExtraMessage = setExtraMessage;
    vm.isTimeDimesion = null;

    init();

    function init() {
      vm.dimension = {};
      vm.dimension.name = dimensionName;
      vm.isTimeDimesion = isTimeDimension;
      vm.dimension.field = fieldName;
      vm.cubeTypes = template.types;
      vm.dimension.type = vm.cubeTypes[0].value;
      vm.precisionOptions = template.precisionOptions;
      vm.defaultType =  vm.cubeTypes[0].value;
      vm.nameError = "";
      vm.dateTimeConfiguration = template.DateTime;
    }

    ///////////////////////////////////////

    function getPrecisionsOfType() {
      var result = $filter('filter')(vm.precisionOptions, {type: vm.dimension.type});
      if (result && result.length > 0) {
        return result[0].precisions;
      }else
      return []
    }

    function cleanPrecision() {
      if (vm.dimension.type == vm.defaultType)
        delete vm.dimension.precision;
    }

    function formatAttributes(){
      if(vm.dimension.type === 'DateTime') {
        vm.dimension.precision = vm.dimension.precisionNumber + vm.dimension.precisionTime;
        delete vm.dimension.precisionNumber;
        delete vm.dimension.precisionTime;
        if (vm.dimension.isTimeDimension) {
          vm.dimension.computeLast = vm.dimension.computeLastNumber + vm.dimension.computeLastTime;
          /* Set unique cube timeDimension */
          vm.isTimeDimesion = true;
          delete vm.dimension.computeLastNumber;
          delete vm.dimension.computeLastTime;
          delete vm.dimension.isTimeDimension;
        }
      }
    }

    function validatePrecision() {
      var validPrecision = (vm.dimension.type == vm.defaultType) || (!(vm.dimension.type == vm.defaultType) && vm.dimension.precision);
      if (!validPrecision) {
        vm.nameError = "_POLICY_._CUBE_._INVALID_DIMENSION_PRECISION_";
      }
      return validPrecision;
    }

    function isRepeated() {
      var position = UtilsService.findElementInJSONArray(dimensions, vm.dimension, "name");
      var repeated = position != -1;
      if (repeated) {
        vm.nameError = "_POLICY_._CUBE_._DIMENSION_NAME_EXISTS_";
        document.querySelector('#nameForm').focus();
      }
      return repeated;
    }

    function setPropertyDisabled (propertyId, isTimeDimension) {
      var disabled = (propertyId === 'isTimeDimension' && isTimeDimension === true)? true : false;
      return disabled;
    }

    function setExtraMessage (isTimeDimesion) {
      var message = (isTimeDimesion)? "_ONE_IS_TIME_DIMENSION_ALLOWED_" : "";
      return message;
    }

    function ok() {
      vm.nameError = "";
      if (vm.form.$valid) {
        formatAttributes();
        if (validatePrecision() && !isRepeated()) {
          cleanPrecision();
          var dimensionData = {};
          dimensionData.dimension = vm.dimension;
          dimensionData.isTimeDimension = vm.isTimeDimesion;
          $modalInstance.close(dimensionData);
        }
      }
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  }

})();
