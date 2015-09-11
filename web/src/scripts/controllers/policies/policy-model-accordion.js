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
    vm.isLastModel = isLastModel;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.policy.models = [];
      vm.newModel = ModelFactory.GetModel();
      vm.accordionStatus = AccordionStatusService.accordionStatus;
      vm.templateModelData = ModelStaticDataFactory;
      AccordionStatusService.ResetAccordionStatus(vm.policy.models.length);
    }

    function addModel() {
      vm.error = false;
      if (isValidModel()) {
        var newModel = angular.copy(vm.newModel);
        newModel.order = vm.policy.models.length + 1;
        vm.policy.models.push(newModel);
        ModelFactory.ResetModel();
        AccordionStatusService.ResetAccordionStatus(vm.policy.models.length);
        AccordionStatusService.accordionStatus.newItem = true;
      } else
        vm.error = true;
    }

    function removeModel(index) {
      if (index == vm.policy.models.length -1){ //only it is possible to remove the last model
      vm.policy.models.splice(index, 1);
      vm.newModelIndex = vm.policy.models.length;
      AccordionStatusService.ResetAccordionStatus(vm.policy.models.length);
      AccordionStatusService.accordionStatus.newItem = true;
      ModelFactory.ResetModel();
      }
    }

    function isValidModel() {
      return vm.newModel.inputField != "" && vm.newModel.outputFields.length > 0 && vm.newModel.configuration != "" &&
        vm.newModel.name != "" && vm.newModel.type != "" && ModelFactory.IsValidConfiguration();
    }

    function getIndex() {
      return index++;
    }

    function isLastModel(index){
      return index == vm.policy.models.length -1;
    }

    function nextStep() {
      PolicyModelFactory.NextStep();
    }
  }
})();
