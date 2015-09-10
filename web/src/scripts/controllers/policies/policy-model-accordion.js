(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelAccordionCtrl', PolicyModelAccordionCtrl);

  PolicyModelAccordionCtrl.$inject = ['PolicyModelFactory', 'ModelStaticDataFactory', 'AccordionStatusService', 'ModelFactory'];

  function PolicyModelAccordionCtrl(PolicyModelFactory, ModelStaticDataFactory, AccordionStatusService, ModelFactory) {
    var vm = this;
    var index = 0;

    vm.init = init;
    vm.addModel = addModel;
    vm.removeModel = removeModel;
    vm.nextStep = nextStep;
    vm.getIndex = getIndex;

    vm.init();


    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.policy.models = ModelFactory.GetModelList();
      vm.newModel = ModelFactory.GetNewModel();
      vm.accordionStatus = AccordionStatusService.accordionStatus;
      vm.templateModelData = ModelStaticDataFactory;
      AccordionStatusService.ResetAccordionStatus(vm.policy.models.length);
    }

    function addModel() {
      vm.error = false;
      if (isValidModel()) {
        var newModel = angular.copy(vm.newModel);
        newModel.order = vm.policy.models.length +1;
        vm.policy.models.push(newModel);
        ModelFactory.ResetNewModel();
        AccordionStatusService.ResetAccordionStatus(vm.policy.models.length);
      } else
        vm.error = true;
    }

    function removeModel(index) {
      vm.policy.models.splice(index, 1);
      vm.newModelIndex = vm.policy.models.length;
      AccordionStatusService.ResetAccordionStatus(vm.policy.models.length);
      AccordionStatusService.accordionStatus.newItem = true;
    }

    function isValidModel() {
      return vm.newModel.inputs.length > 0 && vm.newModel.outputs.length > 0 && vm.newModel.configuration != "" && vm.newModel.type != "";
    }

    function getIndex() {
      return index++;
    }

    function nextStep() {
      PolicyModelFactory.NextStep();
    }
  }
})();
