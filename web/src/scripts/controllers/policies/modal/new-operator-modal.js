(function () {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('NewOperatorModalCtrl', NewOperatorModalCtrl);

  NewOperatorModalCtrl.$inject = ['$modalInstance', 'operatorName', 'operatorType', 'operators','PolicyStaticDataFactory','CubeStaticDataFactory', 'UtilsService'];

  function NewOperatorModalCtrl($modalInstance, operatorName, operatorType, operators,PolicyStaticDataFactory,CubeStaticDataFactory, UtilsService) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;

    init();

    function init(){
      vm.operator = {};
      vm.operator.name = operatorName;
      vm.operator.configuration = "";
      vm.operator.type = operatorType;
      vm.configHelpLink = PolicyStaticDataFactory.getConfigurationHelpLink();
      vm.configPlaceholder = PolicyStaticDataFactory.getConfigPlaceholder();
      vm.operator.configuration = CubeStaticDataFactory.getDefaultOperatorConfiguration();
      vm.error = false;
      vm.errorText = "";
    }

    ///////////////////////////////////////

    function isValidConfiguration() {
      var configuration = vm.operator.configuration;
      try {
        vm.operator.configuration = JSON.parse(configuration);
        return true;
      } catch (e) {
        vm.errorText = "_POLICY_._CUBE_._INVALID_CONFIG_";
        vm.operator.configuration = configuration;
        return false;
      }
    }

    function isRepeated() {
      var position = UtilsService.findElementInJSONArray(operators, vm.operator, "name");
      var repeated = position != -1;
      if (repeated) {
        vm.errorText = "_POLICY_._CUBE_._OPERATOR_NAME_EXISTS_";
      }
      return repeated;
    }

    function ok() {
      vm.errorText = "";
      if (vm.form.$valid && !isRepeated() && isValidConfiguration()) {
        $modalInstance.close(vm.operator);
      }
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
