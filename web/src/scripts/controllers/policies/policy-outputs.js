(function () {
  'use strict';

  /*POLICY OUTPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyOutputCtrl', PolicyOutputCtrl);

  PolicyOutputCtrl.$inject = ['FragmentFactory', 'PolicyModelFactory', 'UtilsService'];

  function PolicyOutputCtrl(FragmentFactory, PolicyModelFactory, UtilsService) {
    var vm = this;
    vm.setOutput = setOutput;
    vm.previousStep = previousStep;
    vm.validateForm = validateForm;

    init();

    ////////////////////////////////////

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.helpLink = vm.template.helpLinks.outputs;
      vm.formSubmmited = false;
      vm.error = false;
      vm.outputList = [];
      vm.policy = PolicyModelFactory.getCurrentPolicy();

      var outputList = FragmentFactory.getFragments("output");
      return outputList.then(function (result) {
        vm.outputList = result;
        sortCurrentOutputs();
      });
    }

    function sortCurrentOutputs() {
      var outputs = [];
      if (vm.policy.outputs) {
        for (var i = 0; i < vm.policy.outputs.length; ++i) {
          var currentOutput = vm.policy.outputs[i];
          var position = UtilsService.findElementInJSONArray(vm.outputList, currentOutput, "id");
          if (position != -1) {
            outputs[position] = currentOutput;
          }
        }
      }
      vm.policy.outputs = outputs;
    }

    function setOutput(index) {
      if (vm.policy.outputs[index]) {
        vm.policy.outputs[index] = null;
      }
      else {
        vm.policy.outputs[index] = vm.outputList[index];
      }
    }

    function previousStep() {
      PolicyModelFactory.previousStep();
    }

    function validateForm() {
      vm.formSubmmited = true;
      var selectedOutputs = checkOutputsSelected();

      if (selectedOutputs > 0) {
        vm.error = false;
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = true;
      }
    }

    function checkOutputsSelected() {
      var outputsCount = 0;
      var outputsLength = vm.policy.outputs.length;

      for (var i = 0; i < outputsLength; ++i) {
        if (vm.policy.outputs[i]) {
          outputsCount++;
        }
      }
      return outputsCount;
    }
  }
})();
