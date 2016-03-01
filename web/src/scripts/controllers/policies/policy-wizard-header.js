(function () {
  'use strict';

  /*POLICY WIZARD HEADER CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyWizardHeaderCtrl', PolicyWizardHeaderCtrl);

  PolicyWizardHeaderCtrl.$inject = ['PolicyModelFactory', 'ModalService'];
  function PolicyWizardHeaderCtrl(PolicyModelFactory, ModalService) {
    var vm = this;

    vm.policy = PolicyModelFactory.getCurrentPolicy();
    vm.showPolicyData = showPolicyData;

    init();

    function init() {
      var policyTemplate =  PolicyModelFactory.getTemplate();
      if (vm.policy && policyTemplate.helpLinks) {
        vm.helpLink = PolicyModelFactory.getTemplate().helpLinks.description;
      }
    }

    function showPolicyData() {
      var controller = 'PolicyCreationModalCtrl';
      var templateUrl = "templates/modal/policy-creation-modal.tpl.html";
      var resolve = {};
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');
    }
  }
})();
