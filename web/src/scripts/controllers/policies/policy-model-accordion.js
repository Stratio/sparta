(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelAccordionCtrl', PolicyModelAccordionCtrl);

  PolicyModelAccordionCtrl.$inject = ['PolicyModelFactory', 'AccordionStatusService',
    'ModelFactory', '$scope'];

  function PolicyModelAccordionCtrl(PolicyModelFactory, AccordionStatusService,
                                    ModelFactory, $scope) {
    var vm = this;
    var index = 0;

    vm.init = init;
    vm.previousStep = previousStep;
    vm.nextStep = nextStep;
    vm.generateIndex = generateIndex;

    vm.init();

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.accordionStatus = AccordionStatusService.getAccordionStatus();
      AccordionStatusService.resetAccordionStatus(vm.policy.transformations.length);
      vm.helpLink = vm.template.helpLinks.models;
      vm.error = "";
    }

    function generateIndex() {
      return index++;
    }

    function previousStep() {
      PolicyModelFactory.previousStep();
    }

    function nextStep() {
      if (vm.policy.transformations.length > 0) {
        vm.error = "";
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = "_POLICY_._MODEL_ERROR_";
      }
    }

    $scope.$watchCollection(
      "vm.accordionStatus",
      function (newValue) {
        if (vm.accordionStatus && newValue) {
          var selectedModelPosition = newValue.indexOf(true);
          if (vm.policy.transformations.length > 0 && selectedModelPosition >= 0 && selectedModelPosition < vm.policy.transformations.length) {
            var selectedModel = vm.policy.transformations[selectedModelPosition];
            ModelFactory.setModel(selectedModel, selectedModelPosition);
          } else {
            var modelNumber = vm.policy.transformations.length;
            var order = 0;

            if (modelNumber > 0) {
              order = vm.policy.transformations[modelNumber - 1].order + 1
            }
            ModelFactory.resetModel(vm.template.model, order, vm.policy.transformations.length);
          }
          ModelFactory.updateModelInputs(vm.policy.transformations);
        }
      }
    );
  }
})();
