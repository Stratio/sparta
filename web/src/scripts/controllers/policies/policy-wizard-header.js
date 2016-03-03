(function () {
  'use strict';

  /*POLICY WIZARD HEADER CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyWizardHeaderCtrl', PolicyWizardHeaderCtrl);

  PolicyWizardHeaderCtrl.$inject = ['PolicyModelFactory', 'ModalService', '$scope'];
  function PolicyWizardHeaderCtrl(PolicyModelFactory, ModalService, $scope) {
    var header = this;

    var policyTemplate = null;
    header.policy = PolicyModelFactory.getCurrentPolicy();
    header.wizardStatus = PolicyModelFactory.getProcessStatus();

    header.showPolicyData = showPolicyData;

    function showPolicyData() {
      var controller = 'PolicyCreationModalCtrl';
      var templateUrl = "templates/modal/policy-creation-modal.tpl.html";
      var resolve = {};
      ModalService.openModal(controller, templateUrl, resolve, '', 'lg');
    }

    $scope.$watchCollection(
      "header.wizardStatus",
      function (newStatus) {
        policyTemplate = PolicyModelFactory.getTemplate();
        if (newStatus && newStatus && newStatus.currentStep >= 0 && newStatus.currentStep  < policyTemplate.helpLinks.length-1 ) {
          header.helpLink = policyTemplate.helpLinks[newStatus.currentStep + 1];
        }else{
          header.helpLink = null;
        }
      }
    );
  }
})();
