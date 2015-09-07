(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelsCtrl', PolicyModelsCtrl);

  PolicyModelsCtrl.$inject = ['policyModelFactory', 'PolicyStaticDataFactory', 'ModelStaticDataFactory', 'AccordionService'];

  function PolicyModelsCtrl(PolicyModelFactory, PolicyStaticDataFactory, ModelStaticDataFactory, AccordionService) {
    var vm = this;
    vm.init = init;
    vm.isCurrentModel = isCurrentModel;
    vm.getModelInputs = getModelInputs;
    vm.addModel = addModel;
    vm.showModel = showModel;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.accordionStatus = [];
      vm.accordionStatus[vm.policy.models.length] = true;
      vm.currentModel = {};
      vm.currentModelIndex = vm.policy.models.length;
      vm.templateModelData = ModelStaticDataFactory.types;
      initNewModel();
    }

    function isCurrentModel(index) {
      return (vm.currentModelIndex == index)
    }

    function getModelInputs() {
      var models = vm.policy.models;
      var result = [];
      if (vm.currentModelIndex >= 0) {
        if (vm.currentModelIndex == 0)
          result = ModelStaticDataFactory.defaultInput;
        else {
          var model = models[--vm.currentModelIndex];
          result = model.outputs;
        }
      }
      return result;
    }

    function addModel() {
      vm.policy.models.push(vm.currentModel);
      vm.currentModelIndex = vm.policy.models.length;
      vm.accordionStatus[vm.policy.models.length] = true;
      initNewModel();
    }

    function initNewModel() {
      vm.currentModel = {};
      vm.currentModel.inputs = vm.getModelInputs();
      vm.currentModel.outputs = [];
    }

  }
})();
