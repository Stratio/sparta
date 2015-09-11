(function() {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewDimensionModalCtrl', NewDimensionModalCtrl);

  NewDimensionModalCtrl.$inject = ['$modalInstance','dimensionName', 'fieldName', 'type'];

  function NewDimensionModalCtrl($modalInstance, dimensionName, fieldName, type) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.dimension = {};
    vm.dimension.name = dimensionName;
    vm.dimension.field = fieldName;
    vm.dimension.type = type;

    ///////////////////////////////////////

    function ok() {
      if (vm.form.$valid) {
        $modalInstance.close(vm.dimension);
      }
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
