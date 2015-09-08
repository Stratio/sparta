(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelsCtrl', PolicyModelsCtrl);

  PolicyModelsCtrl.$inject = ['PolicyModelFactory', 'ModelStaticDataFactory', 'AccordionStatusService'];

  function PolicyModelsCtrl(PolicyModelFactory, ModelStaticDataFactory, AccordionStatusService) {
    var vm = this;
    vm.init = init;
    vm.isCurrentModel = isCurrentModel;
    vm.getModelInputs = getModelInputs;
    vm.addModel = addModel;
    vm.getCurrentModel = getCurrentModel;
    vm.removeModel = removeModel;
    vm.nextStep = nextStep;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.accordionStatus = AccordionStatusService.GetAccordionStatus();
      vm.newModel = {};
      vm.newModelIndex = vm.policy.models.length;
      vm.templateModelData = ModelStaticDataFactory;
      initNewModel();

      AccordionStatusService.ResetAccordionStatus(vm.policy.models.length,vm.policy.models.length);
    }

    function isCurrentModel(index) {
      return (vm.newModelIndex == index)
    }

    function getModelInputs() {
      var models = vm.policy.models;
      var result = [];
      if (vm.newModelIndex >= 0) {
        if (vm.newModelIndex == 0)
          result = ModelStaticDataFactory.defaultInput;
        else {
          var model = models[--vm.newModelIndex];
          result = model.inputs.concat(model.outputs);
        }
      }
      return result;
    }

    function addModel() {
      if (isValidModel()) {
        var newModel = angular.copy(vm.newModel);
        vm.policy.models.push(newModel);
        initNewModel();

        AccordionStatusService.ResetAccordionStatus(vm.policy.models.length,vm.policy.models.length);
      }else
        vm.showModelError = true;
    }

    function initNewModel() {
      vm.newModelIndex = vm.policy.models.length;
      vm.newModel.inputs = vm.getModelInputs();
      vm.newModel.outputs = [];
      vm.newModel.type = "";
      vm.newModel.configuration = "";
      vm.showModelError = false;
    }

    function getCurrentModel(index) {
      if (vm.policy.models.length == 0) {
        return vm.newModel;
      } else
        return vm.policy.models[index]
    }

    function removeModel(index) {
      vm.policy.models.splice(index, 1);
      vm.newModelIndex = vm.policy.models.length;
      AccordionStatusService.ResetAccordionStatus(vm.policy.models.length,vm.policy.models.length);
    }

    function isValidModel() {
      return vm.newModel.inputs.length > 0  && vm.newModel.outputs.length > 0 && vm.newModel.configuration != "" && vm.newModel.type != "";
    }

    function nextStep(){
      PolicyModelFactory.NextStep();
    }
  }
})();
