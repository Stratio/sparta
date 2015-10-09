(function () {
  'use strict';

  /*POLICY CREATION CONTROLLER*/
  angular
    .module('webApp')
    .controller('NewPolicyCtrl', NewPolicyCtrl);

  NewPolicyCtrl.$inject = ['TemplateFactory', 'PolicyModelFactory', 'PolicyFactory', 'ModalService', '$state'];
  function NewPolicyCtrl(TemplateFactory, PolicyModelFactory, PolicyFactory, ModalService, $state) {
    var vm = this;

    vm.confirmPolicy = confirmPolicy;

    init();

    function init() {
      return TemplateFactory.getPolicyTemplate().then(function (template) {
        PolicyModelFactory.setTemplate(template);
        vm.steps = template.steps;
        PolicyModelFactory.resetPolicy();
        vm.policy = PolicyModelFactory.getCurrentPolicy();
        vm.status = PolicyModelFactory.getProcessStatus();
        vm.successfullySentPolicy = false;
        vm.error = null;
      });
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
