(function () {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewOperatorModalCtrl', NewOperatorModalCtrl);

  NewOperatorModalCtrl.$inject = ['$modalInstance', 'operatorName', 'operatorType', 'PolicyStaticDataFactory'];

  function NewOperatorModalCtrl($modalInstance, operatorName, operatorType, PolicyStaticDataFactory) {
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
    vm.error = false;
    ///////////////////////////////////////

    function isValidConfiguration() {
      var configuration = vm.operator.configuration;
      try {
        vm.operator.configuration = JSON.parse(configuration);
        return true;
      } catch (e) {
        vm.operator.configuration = configuration;
        return false;
      }
    }

    function ok() {
      if (isValidConfiguration())
        $modalInstance.close(vm.operator);
      else {

        vm.error = true;
        vm.errorText = "_GENERIC_FORM_ERROR_";
      }
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
