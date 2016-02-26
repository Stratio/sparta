(function () {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewDimensionModalCtrl', NewDimensionModalCtrl);

  NewDimensionModalCtrl.$inject = ['$modalInstance', 'dimensionName', 'fieldName', 'dimensions',
    '$filter', 'UtilsService', 'template'];

  function NewDimensionModalCtrl($modalInstance, dimensionName, fieldName, dimensions, $filter, UtilsService, template) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.getPrecisionsOfType = getPrecisionsOfType;

    init();

    function init() {
      vm.dimension = {};
      vm.dimension.name = dimensionName;
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
      console.log(vm.dimension);
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
      }
      return repeated;
    }

    function ok() {
      vm.errorText = "";
      if (vm.form.$valid) {
        if (validatePrecision() && !isRepeated()) {
          cleanPrecision();
          $modalInstance.close(vm.dimension);
        }
      }
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  }

})();
