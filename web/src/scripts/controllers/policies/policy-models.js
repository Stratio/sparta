(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelsCtrl', PolicyModelsCtrl);

  PolicyModelsCtrl.$inject = ['policyModelFactory', 'ModelStaticDataFactory'];

  function PolicyModelsCtrl(PolicyModelFactory,  ModelStaticDataFactory) {
    var vm = this;
    vm.init = init;
    vm.isCurrentModel = isCurrentModel;
    vm.getModelInputs = getModelInputs;
    vm.addModel = addModel;
    vm.getCurrentModel = getCurrentModel;
    vm.removeModel = removeModel;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.accordionStatus = [];
      vm.newModel = {};
      vm.newModelIndex = vm.policy.models.length;
      vm.templateModelData = ModelStaticDataFactory.types;
      initNewModel();
      resetAccordionStatus();
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
          result = model.outputs;
        }
      }
      return result;
    }

    function addModel() {
      var newModel = angular.copy(vm.newModel);
      vm.policy.models.push(newModel);
      initNewModel();
      resetAccordionStatus();
    }

    function initNewModel() {
      vm.newModelIndex = vm.policy.models.length;
      vm.newModel.inputs = vm.getModelInputs();
      vm.newModel.outputs = [];
      vm.newModel.type = "";
      vm.newModel.configuration = "";
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
      resetAccordionStatus();
    }

    function resetAccordionStatus() {
      for (var i = 0; i < vm.policy.models.length; ++i) {
        vm.accordionStatus[i] = false;
      }
      vm.accordionStatus[vm.policy.models.length] = true;
    }
  }
})();
