(function () {
  'use strict';

  /*POLICY INPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyInputCtrl', PolicyInputCtrl);

  PolicyInputCtrl.$inject = ['FragmentFactory', 'PolicyModelFactory'];

  function PolicyInputCtrl(FragmentFactory, PolicyModelFactory) {
    var vm = this;
    vm.setInput = setInput;
    vm.isSelectedInput = isSelectedInput;
    vm.previousStep = previousStep;
    vm.validateForm = validateForm;
    vm.inputList = [];
    vm.error = false;
    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      if (Object.keys(vm.policy).length > 0) {
        vm.template = PolicyModelFactory.getTemplate();
        if (vm.policy &&  Object.keys(vm.template).length > 0) {
          vm.helpLink = vm.template.helpLinks.inputs;
          var inputList = FragmentFactory.getFragments("input");
          return inputList.then(function (result) {
            vm.inputList = result;
          });
        }
      }
    }

    function setInput(index) {
      if (index >= 0 && index < vm.inputList.length) {
        vm.policy.input = vm.inputList[index];
        vm.error = false
      }
    }

    function isSelectedInput(name) {
      if (vm.policy.input)
        return name == vm.policy.input.name;
      else
        return false;
    }

    function previousStep() {
      PolicyModelFactory.previousStep();
    }

    function validateForm() {
      if (vm.policy.input.name) {
        vm.error = false;
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = true;
      }
    }
  }
})();
