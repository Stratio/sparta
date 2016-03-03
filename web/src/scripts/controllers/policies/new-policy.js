(function () {
  'use strict';

  /*POLICY CREATION CONTROLLER*/
  angular
    .module('webApp')
    .controller('NewPolicyCtrl', NewPolicyCtrl);

  NewPolicyCtrl.$inject = ['PolicyModelFactory', 'PolicyFactory', 'ModalService', '$state'];
  function NewPolicyCtrl(PolicyModelFactory, PolicyFactory, ModalService, $state) {
    var vm = this;

    vm.changeStepNavigationVisibility = changeStepNavigationVisibility;
    vm.confirmPolicy = confirmPolicy;
    vm.closeErrorMessage = closeErrorMessage;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      if (vm.policy && PolicyModelFactory.getProcessStatus().currentStep == 0) {
        vm.steps = PolicyModelFactory.getTemplate().steps;
        vm.status = PolicyModelFactory.getProcessStatus();
        vm.successfullySentPolicy = false;
        vm.error = null;
        vm.showStepNavigation = true;
        vm.editionMode  = false;
      }
      else {
        $state.go('dashboard.policies');
      }
    }

    function closeErrorMessage() {
      vm.error = null;
    }

    function changeStepNavigationVisibility() {
      vm.showStepNavigation = !vm.showStepNavigation;
    }

    function confirmPolicy() {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var resolve = {
        title: function () {
          return "_POLICY_._WINDOW_._CONFIRM_._TITLE_";
        },
        message: function () {
          return "";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, "", "lg");

      return modalInstance.result.then(function () {
        var finalJSON = PolicyModelFactory.getFinalJSON();
        PolicyFactory.createPolicy(finalJSON).then(function () {
          PolicyModelFactory.resetPolicy();
          $state.go("dashboard.policies");
        }, function (error) {
          if (error) {
            if (error.data.message) {
              vm.error = error.data.message;
            }
            else
             vm.error = error.data;
          }
        });
      });
    }
  }
})();
