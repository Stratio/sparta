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
      vm.errorText = "";
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

    function validatePrecision() {
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
      var validPrecision = (vm.dimension.type == vm.defaultType) || (!(vm.dimension.type == vm.defaultType) && vm.dimension.precision);
      if (!validPrecision) {
        vm.errorText = "_POLICY_._CUBE_._INVALID_DIMENSION_PRECISION_";
      }
      return validPrecision;
    }

    function isRepeated() {
      var position = UtilsService.findElementInJSONArray(dimensions, vm.dimension, "name");
      var repeated = position != -1;
      if (repeated) {
        vm.errorText = "_POLICY_._CUBE_._DIMENSION_NAME_EXISTS_";
        document.querySelector('#nameForm').focus();
      }
      return repeated;
    }

    function setPropertyDisabled (propertyId, isTimeDimesion) {
      var disabled = (propertyId === 'isTimeDimension' && isTimeDimesion === true)? true : false;
      return disabled;
    }

    function setExtraMessage (isTimeDimesion) {
      var message = (isTimeDimesion)? "_ONE_IS_TIME_DIMENSION_ALLOWED_" : "";
      return message;
    }

    function ok() {
      vm.errorText = "";
      if (vm.form.$valid) {
        if (validatePrecision() && !isRepeated()) {
          cleanPrecision();
          var dimesionData = {};
          dimesionData.dimesion = vm.dimension;
          dimesionData.isTimeDimesion = vm.isTimeDimesion;
          $modalInstance.close(dimesionData);
        }
      }
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  }

})();
