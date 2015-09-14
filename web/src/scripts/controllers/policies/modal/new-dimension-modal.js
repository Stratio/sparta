(function () {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewDimensionModalCtrl', NewDimensionModalCtrl);

  NewDimensionModalCtrl.$inject = ['$modalInstance', 'dimensionName', 'fieldName', 'type', 'CubeStaticDataFactory', '$filter'];

  function NewDimensionModalCtrl($modalInstance, dimensionName, fieldName, type, CubeStaticDataFactory, $filter) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.getPrecisionsOfType = getPrecisionsOfType;

    vm.dimension = {};
    vm.dimension.name = dimensionName;
    vm.dimension.field = fieldName;
    vm.dimension.type = type;
    vm.precisionOptions = CubeStaticDataFactory.getPrecisionOptions();
    vm.cubeTypes = CubeStaticDataFactory.getCubeTypes();
    vm.defaultType = CubeStaticDataFactory.getDefaultType().value;

    ///////////////////////////////////////

    function getPrecisionsOfType() {
      var result = $filter('filter')(vm.precisionOptions, {type: vm.dimension.type});
      if (result && result.length > 0) {
        return result[0].precisions;
      }
    };

    function cleanPrecision() {
      if (vm.dimension.type == vm.defaultType)
        delete vm.dimension.precision;
    }

    function ok() {
      if (vm.form.$valid) {
        cleanPrecision();
        $modalInstance.close(vm.dimension);
      }
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
