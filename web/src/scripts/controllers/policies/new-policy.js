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

    init();

    function init() {
      var controller = 'PolicyCreationModalCtrl';
      var templateUrl = "templates/modal/policy-creation-modal.tpl.html";
      var resolve = {};
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');
      modalInstance.result.then(function () {
        vm.steps = PolicyModelFactory.getTemplate().steps;
        vm.policy = PolicyModelFactory.getCurrentPolicy();
        vm.status = PolicyModelFactory.getProcessStatus();
        vm.successfullySentPolicy = false;
        vm.error = null;
        vm.showStepNavigation = true;
      });
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
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);

      return modalInstance.result.then(function () {
        var finalJSON = PolicyModelFactory.getFinalJSON();
        PolicyFactory.createPolicy(finalJSON).then(function () {
          PolicyModelFactory.resetPolicy();
          $state.go("dashboard.policies");
        }, function (error) {
          if (error) {
            vm.error = error.data;
          }
        });
      });
    }
  }
})();
