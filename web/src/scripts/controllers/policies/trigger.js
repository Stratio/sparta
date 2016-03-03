(function () {
  'use strict';

  /*TRIGGER CONTROLLER*/
  angular
    .module('webApp')
    .controller('TriggerCtrl', TriggerCtrl);

  TriggerCtrl.$inject = ['PolicyModelFactory', 'TriggerModelFactory', 'TriggerService', 'OutputService'];

  function TriggerCtrl(PolicyModelFactory, TriggerModelFactory, TriggerService, OutputService) {
    var vm = this;

    vm.init = init;
    vm.addTrigger = addTrigger;
    vm.addOutput = addOutput;
    vm.changeSqlHelpVisibility = changeSqlHelpVisibility;

    vm.removeTrigger = TriggerService.removeTrigger;
    vm.isNewTrigger = TriggerService.isNewTrigger;
    vm.saveTrigger = TriggerService.saveTrigger;

    vm.init();

    function init() {
      vm.trigger = TriggerModelFactory.getTrigger();
      if (vm.trigger) {
        vm.trigger.overLastNumber = PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowNumber;
        vm.trigger.overLastTime = PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowTime;
        vm.triggerContext = TriggerModelFactory.getContext();
        vm.template = PolicyModelFactory.getTemplate().trigger;
        vm.outputsHelpLinks = PolicyModelFactory.getTemplate().helpLinks[4];
        vm.showSqlHelp = false;
        if (TriggerService.isEnabledHelpForSql()) {
          vm.sqlSourceItems = TriggerService.getSqlHelpSourceItems();
        }
        return OutputService.generateOutputNameList().then(function (outputList) {
          vm.policyOutputList = outputList;
        });
      }
    }

    function changeSqlHelpVisibility() {
      vm.showSqlHelp = !vm.showSqlHelp;
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
