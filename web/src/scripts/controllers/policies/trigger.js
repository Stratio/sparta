(function () {
  'use strict';

  /*TRIGGER CONTROLLER*/
  angular
    .module('webApp')
    .controller('TriggerCtrl', TriggerCtrl);

  TriggerCtrl.$inject = ['PolicyModelFactory','TriggerModelFactory', 'TriggerService', 'ModalService', 'OutputService'];

  function TriggerCtrl(PolicyModelFactory,TriggerModelFactory, TriggerService,  ModalService, OutputService) {
    var vm = this;

    vm.init = init;
    vm.addTrigger = addTrigger;
    vm.addOutput = addOutput;
    vm.removeTrigger = TriggerService.removeTrigger;
    vm.isNewTrigger = TriggerService.isNewTrigger;
    vm.saveTrigger = TriggerService.saveTrigger;

    vm.init();

    function init() {
      vm.trigger = TriggerModelFactory.getTrigger();
      if (vm.trigger) {
        vm.template = PolicyModelFactory.getTemplate().trigger;
        return OutputService.generateOutputList().then(function (outputList) {
          vm.policyOutputList = outputList;
        });
      }
    }

    function showConfirmModal(title, message) {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var resolve = {
        title: function () {
          return title
        },
        message: function () {
          return message;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);
      return modalInstance.result;
    }

    function addTrigger() {
      vm.form.$submitted = true;
      if (vm.form.$valid && vm.trigger.operators.length > 0 && vm.trigger.dimensions.length > 0) {
        vm.form.$submitted = false;
        TriggerService.addTrigger();
        TriggerService.changeTriggerCreationPanelVisibility(false);
      }
      else {
        TriggerModelFactory.setError();
      }
    }

    function addOutput() {
      if (vm.selectedPolicyOutput && vm.trigger.outputs.indexOf(vm.selectedPolicyOutput) == -1) {
        vm.trigger.outputs.push(vm.selectedPolicyOutput);
      }
    }
  }
})();
