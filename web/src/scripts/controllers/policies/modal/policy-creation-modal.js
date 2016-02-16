(function () {
  'use strict';

  /*POLICY CREATION MODAL CONTROLLER */
  angular
    .module('webApp')
    .controller('PolicyCreationModalCtrl', PolicyCreationModalCtrl);

  PolicyCreationModalCtrl.$inject = ['PolicyModelFactory','PolicyFactory', 'TemplateFactory', '$modalInstance'];

  function PolicyCreationModalCtrl(PolicyModelFactory, PolicyFactory, TemplateFactory, $modalInstance) {
    /*jshint validthis: true*/
    var vm = this;
    vm.ok = ok;
    vm.cancel = cancel;
    vm.validateForm = validateForm;

    init();

    ///////////////////////////////////////

    function init() {
      return TemplateFactory.getPolicyTemplate().then(function (template) {
        PolicyModelFactory.setTemplate(template);
        PolicyModelFactory.resetPolicy();
        vm.policy = PolicyModelFactory.getCurrentPolicy();
        vm.template = template;
      });
    }

    function validateForm() {
      if (vm.form.$valid) {
        vm.error = false;
        /*Check if the name of the policy already exists*/
        return PolicyFactory.existsPolicy(vm.policy.name).then(function (found) {
          vm.error = found;
          if (!found) {
            vm.policy.rawData.enabled = vm.policy.rawData.enabled.toString();
            $modalInstance.close();
          }
        });
      }
    }

    function ok() {
      $modalInstance.close();
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  }

})();
