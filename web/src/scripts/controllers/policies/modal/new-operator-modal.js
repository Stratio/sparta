(function () {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewOperatorModalCtrl', NewOperatorModalCtrl);

  NewOperatorModalCtrl.$inject = ['$modalInstance', 'operatorName','operatorType', 'PolicyStaticDataFactory'];

  function NewOperatorModalCtrl($modalInstance, operatorName, operatorType,PolicyStaticDataFactory) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.operator = {};
    vm.operator.name = operatorName;
    vm.operator.configuration = "";
    vm.operator.type = operatorType;
    vm.configHelpLink = PolicyStaticDataFactory.configurationHelpLink;
    vm.configPlaceholder = PolicyStaticDataFactory.configPlaceholder;

    ///////////////////////////////////////

    function ok() {
      $modalInstance.close(vm.operator);
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
