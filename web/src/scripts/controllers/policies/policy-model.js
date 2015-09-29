(function () {
  'use strict';

  /*POLICY MODEL CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelCtrl', PolicyModelCtrl);

  PolicyModelCtrl.$inject = ['ModelFactory', 'PolicyModelFactory'];

  function PolicyModelCtrl(ModelFactory, PolicyModelFactory) {
    var vm = this;
    vm.init = init;
    vm.changeDefaultConfiguration = changeDefaultConfiguration;

    vm.init();

    function init() {
      vm.model = ModelFactory.getModel();
      if (vm.model) {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.template = PolicyModelFactory.getTemplate();
      vm.modelError = ModelFactory.getError();

      vm.modelTypes = vm.template.types;
      vm.showModelError = false;
      vm.configPlaceholder = vm.template.configPlaceholder;
      vm.outputPattern = vm.template.outputPattern;
      vm.outputInputPlaceholder = vm.template.outputInputPlaceholder;
      }
    }

    function changeDefaultConfiguration() {
      var configString = JSON.stringify(getDefaultConfigurations(vm.model.type), null, 4);
      vm.model.configuration = configString;
    }

    function getDefaultConfigurations(type) {
      var types = vm.template.types;
      switch (type) {
        case types[0].name:
        {
          return vm.template.morphlinesDefaultConfiguration;
        }
        case types[1].name:
        {
          return vm.template.dateTimeDefaultConfiguration;
        }
        case types[2].name:
        {
          return vm.template.typeDefaultConfiguration;
        }
      }
    }


  }
})
();
