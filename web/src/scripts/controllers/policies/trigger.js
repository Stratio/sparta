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
        vm.triggerContext = TriggerModelFactory.getContext();
        vm.template = PolicyModelFactory.getTemplate().trigger;
        return OutputService.generateOutputList().then(function (outputList) {
          vm.policyOutputList = outputList;
        });
      }
    }

    function addTrigger() {
      vm.form.$submitted = true;
      if (vm.form.$valid) {
        vm.form.$submitted = false;
        TriggerService.addTrigger();
        TriggerService.disableTriggerCreationPanel(false);
      }
    }

    function addOutput() {
      if (vm.selectedPolicyOutput && vm.trigger.outputs.indexOf(vm.selectedPolicyOutput) == -1) {
        vm.trigger.outputs.push(vm.selectedPolicyOutput);
      }
    }
  }
})();
