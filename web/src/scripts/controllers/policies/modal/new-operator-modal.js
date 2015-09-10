(function() {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewOperatorModalCtrl', NewOperatorModalCtrl);

  NewOperatorModalCtrl.$inject = ['$modalInstance', 'functionName', 'CubeStaticDataFactory'];

  function NewOperatorModalCtrl($modalInstance, functionName, CubeStaticDataFactory) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.operator = {};
    vm.operator.name = functionName;
    vm.operator.configuration = "";
    vm.operator.type = "";
    vm.configHelpLink =CubeStaticDataFactory.GetConfigurationHelpLink();

    ///////////////////////////////////////

    function ok() {
      $modalInstance.close(vm.operator);
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
