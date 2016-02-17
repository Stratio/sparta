(function () {
  'use strict';

  /*POLICY MODEL CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelCtrl', PolicyModelCtrl);

  PolicyModelCtrl.$inject = ['ModelFactory', 'PolicyModelFactory', 'ModelService'];

  function PolicyModelCtrl(ModelFactory, PolicyModelFactory, ModelService) {
    var vm = this;

    vm.init = init;
    vm.changeDefaultConfiguration = changeDefaultConfiguration;
    vm.addModel = addModel;
    vm.removeModel = removeModel;
    vm.isLastModel = ModelService.isLastModel;
    vm.isNewModel = ModelService.isNewModel;

    vm.modelInputs = ModelFactory.getModelInputs();
    vm.init();

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.model = ModelFactory.getModel();
      vm.modelError = '';
      if (vm.model) {
        vm.modelError = ModelFactory.getError();
        vm.modelContext = ModelFactory.getContext();
        vm.modelTypes = vm.template.model.types;
        vm.configPlaceholder = vm.template.configPlaceholder;
        vm.outputPattern = vm.template.outputPattern;
        vm.outputInputPlaceholder = vm.template.outputInputPlaceholder;
        vm.outputFieldTypes =  vm.template.model.outputFieldTypes;
      }
    }

    function changeDefaultConfiguration() {
      vm.model.configuration = getDefaultConfigurations(vm.model.type);
    }

    function getDefaultConfigurations(type) {
      var types = vm.template.model.types;
      var defaultConfigurations =  vm.template.model.defaultConfiguration;
      switch (type) {
        case types[0].name:
        {
          return defaultConfigurations.morphlinesDefaultConfiguration;
        }
        case types[1].name:
        {
          return defaultConfigurations.dateTimeDefaultConfiguration;
        }
        case types[2].name:
        {
          return defaultConfigurations.typeDefaultConfiguration;
        }
      }
    }

    function addModel() {
      vm.form.$submitted = true;
      if (vm.form.$valid && vm.model.outputFields.length != 0) {
        vm.form.$submitted = false;
        ModelService.addModel();

        ModelService.changeModelCreationPanelVisibility(false);
      } else {
        ModelFactory.setError("_GENERIC_FORM_ERROR_");
      }
    }

    function removeModel() {
      return  ModelService.removeModel().then(function () {
        var order = 0;
        var modelNumber = vm.policy.transformations.length;
        if (modelNumber > 0) {
          order = vm.policy.transformations[modelNumber - 1].order + 1
        }
        vm.model = ModelFactory.resetModel(vm.template.model, order, modelNumber);
        ModelFactory.updateModelInputs(vm.policy.transformations);
      });
    }
  }
})
();
