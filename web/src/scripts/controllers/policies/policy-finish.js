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
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      generateFinalJSON();
      vm.testingpolcyData = JSON.stringify(vm.policy, null, 4);
    };

    function generateFinalJSON() {
      var fragments = [];

      vm.policy.transformations = vm.policy.models;
      fragments.push(vm.policy.input);
      for (var i = 0; i < vm.policy.outputs.length; ++i) {
        if (vm.policy.outputs[i]) {
          fragments.push(vm.policy.outputs[i]);
        }
      }
      vm.policy.fragments = fragments;
      cleanPolicyJSON();

      return vm.policy;
    }

    function cleanPolicyJSON() {
      delete vm.policy.models;
      delete vm.policy.input;
      delete vm.policy.outputs;
      if (vm.policy.rawData.enabled === 'false') {
        delete vm.policy.rawData['path'];
        delete vm.policy.rawData['partitionFormat'];
      }
    }
  };
})();
