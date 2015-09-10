(function () {
  'use strict';

  /*POLICY MODEL CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelCtrl',PolicyModelCtrl);

  PolicyModelCtrl.$inject = ['ModelFactory','PolicyModelFactory','ModelStaticDataFactory'];

  function PolicyModelCtrl(ModelFactory,PolicyModelFactory, ModelStaticDataFactory) {
    var vm = this;
    vm.init = init;

    vm.showModelError = false;

    vm.init();

    function init(model) {
      if (model)
        vm.model = model;
      else vm.model = ModelFactory.GetNewModel();
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.templateModelData = ModelStaticDataFactory;
    }


  }
})();
