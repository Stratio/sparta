(function () {
  'use strict';

  /*POLICY INPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyFinishCtrl', PolicyFinishCtrl);

  PolicyFinishCtrl.$inject = ['PolicyModelFactory'];

  function PolicyFinishCtrl(PolicyModelFactory) {
    var vm = this;

    init();

    ///////////////////////////////////////

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      var json = getFinalJSON();
      vm.testingpolcyData = JSON.stringify(json, null, 4);
    };

    function getFinalJSON() {
      var fragments = [];

      vm.policy.transformations = vm.policy.models;
      fragments.push(vm.policy.input);
      for (var i = 0; i < vm.policy.outputs.length; ++i) {
        if (vm.policy.outputs[i]) {
          fragments.push(vm.policy.outputs[i]);
        }
      }
      vm.policy.fragments = fragments;
      delete vm.policy.models;
      delete vm.policy.input;
      delete vm.policy.outputs;

      return vm.policy;
    }
  };
})();
