(function() {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewOperatorModalCtrl', NewOperatorModalCtrl);

  NewOperatorModalCtrl.$inject = ['$modalInstance', 'functionName'];

  function NewOperatorModalCtrl($modalInstance, functionName) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.operator = {};
    vm.operator.name = functionName;
    vm.operator.configuration = "";
    vm.operator.type = "";

    ///////////////////////////////////////

    function ok() {
      $modalInstance.close(vm.operator);
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
