(function () {
  'use strict';

  /*POLICY WIZARD HEADER CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyWizardHeaderCtrl', PolicyWizardHeaderCtrl);

  PolicyWizardHeaderCtrl.$inject = ['PolicyModelFactory'];
  function PolicyWizardHeaderCtrl(PolicyModelFactory) {
    var vm = this;

    vm.policy = PolicyModelFactory.getCurrentPolicy();

    init();

    function init() {
      var policyTemplate =  PolicyModelFactory.getTemplate();
      if (vm.policy && policyTemplate.helpLinks) {
        vm.policyName = vm.policy.name;
        vm.helpLink = PolicyModelFactory.getTemplate().helpLinks.description;
      }
    }
  }
})();
