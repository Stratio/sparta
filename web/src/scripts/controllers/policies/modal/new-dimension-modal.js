(function() {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewDimensionModalCtrl', NewDimensionModalCtrl);

  NewDimensionModalCtrl.$inject = ['$modalInstance', 'fieldName'];

  function NewDimensionModalCtrl($modalInstance, fieldName) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.dimension = {};
    vm.dimension.name = fieldName;

    ///////////////////////////////////////

    function ok() {
      $modalInstance.close(vm.dimension);
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
