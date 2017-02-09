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
(function() {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
      .module('webApp')
      .controller('NewDimensionModalCtrl', NewDimensionModalCtrl);

  NewDimensionModalCtrl.$inject = ['TemplateFactory', '$uibModalInstance', 'dimensionName', 'fieldName', 'dimensions',
    'UtilsService', 'isTimeDimension', '$scope'];

  function NewDimensionModalCtrl(TemplateFactory, $uibModalInstance, dimensionName, fieldName, dimensions,
                                 UtilsService, isTimeDimension, $scope) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.isEnabledProperty = isEnabledProperty;
    vm.setExtraMessage = setExtraMessage;

    init();

    function init() {
      vm.dimension = {};
      vm.dimension.name = dimensionName;
      vm.isTimeDimension = isTimeDimension;
      vm.dimension.field = fieldName;
      vm.defaultType = 'Default';
      vm.dimension.type =  vm.defaultType;
      vm.error = "";
      vm.loading = false;

      onChangeDimensionType();
    }

    ///////////////////////////////////////
    
    function onChangeDimensionType() {
      TemplateFactory.getDimensionTemplateByType(vm.dimension.type).then(function(template) {
        vm.template = template;
        vm.form.$commitViewValue();
      });
    }

    function cleanPrecision() {
      if (vm.dimension.type == vm.defaultType) {
        delete vm.dimension.precision;
        delete vm.dimension.precisionNumber;
        delete vm.dimension.precisionTime;
      }
    }

    function formatAttributes() {
      if (vm.dimension.type === 'DateTime') {
        vm.dimension.precision = vm.dimension.precisionNumber + vm.dimension.precisionTime;
        delete vm.dimension.precisionNumber;
        delete vm.dimension.precisionTime;
        if (vm.dimension.isTimeDimension) {
          vm.dimension.computeLast = vm.dimension.computeLastNumber + vm.dimension.computeLastTime;
          /* Set unique cube timeDimension */
          vm.isTimeDimension = true;
          delete vm.dimension.computeLastNumber;
          delete vm.dimension.computeLastTime;
          delete vm.dimension.isTimeDimension;
        }
      }
    }

    function validatePrecision() {
      var validPrecision = (vm.dimension.type == vm.defaultType) || (vm.dimension.type != vm.defaultType && vm.dimension.precision != '');
      if (!validPrecision) {
        vm.error = "_POLICY_._CUBE_._INVALID_DIMENSION_PRECISION_";
      }
      return validPrecision;
    }

    function isRepeated() {
      var position = UtilsService.findElementInJSONArray(dimensions, vm.dimension, "name");
      var repeated = position != -1;
      if (repeated) {
        vm.error = "_POLICY_._CUBE_._DIMENSION_NAME_EXISTS_";
        document.querySelector('#name').focus();
      }
      return repeated;
    }

    function isEnabledProperty(propertyId) {
      return propertyId !== 'isTimeDimension' || !vm.isTimeDimension;
    }

    function setExtraMessage(isTimeDimesion) {
      var message = (isTimeDimesion) ? "_ONE_IS_TIME_DIMENSION_ALLOWED_" : "";
      return message;
    }

    function ok() {
      vm.error = "";
      if (vm.form.$valid) {
        cleanPrecision();
        if (validatePrecision() && !isRepeated()) {
          formatAttributes();
          var dimensionData = {};
          dimensionData.dimension = UtilsService.convertDottedPropertiesToJson(vm.dimension);
          dimensionData.isTimeDimension = vm.isTimeDimension;
          $uibModalInstance.close(dimensionData);
        }
      }
    }

    function cancel() {
      $uibModalInstance.dismiss('cancel');
    }

    $scope.$watch('vm.dimension.type', function(newDimensionType, oldDimensionType) {
      if (vm.dimension.type && (oldDimensionType !== vm.dimension.type)) {
        onChangeDimensionType();
      }
    });
  }

})();
