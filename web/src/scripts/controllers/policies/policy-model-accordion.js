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
      //ModelFactory.resetModel(vm.template);
      vm.accordionStatus = AccordionStatusService.getAccordionStatus();
      AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
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
      if (vm.policy.models.length > 0) {
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

        if (vm.accordionStatus) {
          var selectedModelPosition = newValue.indexOf(true);
          var position = null;
          if (vm.policy.models.length > 0 && selectedModelPosition >= 0 && selectedModelPosition < vm.policy.models.length) {
            var selectedModel = vm.policy.models[selectedModelPosition];
            ModelFactory.setModel(selectedModel);
            position = selectedModelPosition;
          } else {
            var modelNumber = vm.policy.models.length;
            var order = 0;

            if (modelNumber > 0) {
              order = vm.policy.models[modelNumber - 1].order + 1
            }
            ModelFactory.resetModel(vm.template, order);
            position = vm.policy.models.length;
          }
          ModelFactory.setPosition(position);
          ModelFactory.updateModelInputs(vm.policy.models);
        }
      }
    );
  }
})();
