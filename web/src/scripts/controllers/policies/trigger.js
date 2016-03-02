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
    vm.removeTrigger = TriggerService.removeTrigger;
    vm.isNewTrigger = TriggerService.isNewTrigger;
    vm.saveTrigger = TriggerService.saveTrigger;
    vm.changeSqlHelpVisibility = changeSqlHelpVisibility;

    vm.init();

    function init() {
      vm.trigger = TriggerModelFactory.getTrigger();
      if (vm.trigger) {
        //vm.trigger.overLast = PolicyModelFactory.getCurrentPolicy().streamWindow; //TODO Change stream window to number and select
        vm.triggerContext = TriggerModelFactory.getContext();
        vm.template = PolicyModelFactory.getTemplate().trigger;
        vm.templateHelpLinks = PolicyModelFactory.getTemplate().helpLinks;
        vm.showSqlHelp = false;
        if (TriggerService.isEnabledHelpForSql()) {
          vm.sqlSourceItems = TriggerService.getSqlHelpSourceItems();
        }
        return OutputService.generateOutputList().then(function (outputList) {
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
