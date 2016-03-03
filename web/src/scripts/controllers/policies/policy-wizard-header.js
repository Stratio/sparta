(function () {
  'use strict';

  /*POLICY WIZARD HEADER CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyWizardHeaderCtrl', PolicyWizardHeaderCtrl);

  PolicyWizardHeaderCtrl.$inject = ['PolicyModelFactory', 'ModalService', '$scope', '$state'];
  function PolicyWizardHeaderCtrl(PolicyModelFactory, ModalService, $scope, $state) {
    var header = this;

    var policyTemplate = null;
    header.policy = PolicyModelFactory.getCurrentPolicy();
    header.wizardStatus = PolicyModelFactory.getProcessStatus();
    header.leaveEditor = leaveEditor;

    header.showPolicyData = showPolicyData;

    function showPolicyData() {
      var controller = 'PolicyCreationModalCtrl';
      var templateUrl = "templates/modal/policy-creation-modal.tpl.html";
      var resolve = {
        title: function () {
          return "_POLICY_._MODAL_SETTINGS_TITLE_";
        }
      };
      ModalService.openModal(controller, templateUrl, resolve, '', 'lg');
    }

    function leaveEditor() {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var resolve = {
        title: function () {
          return "_POLICY_._WINDOW_._EXIT_._TITLE_";
        },
        message: function () {
          return "_POLICY_._EXIT_CONFIRMATION_";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      return modalInstance.result.then(function () {
        $state.go('dashboard.policies');
      });
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
