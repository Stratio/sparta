(function () {
  'use strict';

  /*POLICY OUTPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyOutputCtrl', PolicyOutputCtrl);

  PolicyOutputCtrl.$inject = ['FragmentFactory', 'PolicyModelFactory', '$q', 'PolicyStaticDataFactory'];

  function PolicyOutputCtrl(FragmentFactory, PolicyModelFactory, $q, PolicyStaticDataFactory) {
    var vm = this;
    vm.setOutput = setOutput;
    vm.validateForm = validateForm;

    vm.formSubmmited = false;
    vm.error = false;
    vm.outputList = [];
    init();

    ////////////////////////////////////

    function init() {
      vm.helpLink = PolicyStaticDataFactory.helpLinks.outputs;

      var defer = $q.defer();
      vm.policy = PolicyModelFactory.GetCurrentPolicy();

      var outputList = FragmentFactory.GetFragments("output");
      outputList.then(function (result) {
        vm.outputList = result;
        defer.resolve();
      }, function () {
        defer.reject();
      });
      return defer.promise;
    };

    function setOutput(index) {
      if (vm.policy.outputs[index]) {
        vm.policy.outputs[index] = null;
      }
      else {
        vm.policy.outputs[index] = vm.outputList[index];
      }

      var outputsSelected = checkOutputsSelected();
      vm.error = (outputsSelected>0)? false : true;
    };

    function validateForm() {
      vm.formSubmmited = true;
      var outputsSelected = checkOutputsSelected();

      if (outputsSelected > 0) {
        vm.error = false;
        PolicyModelFactory.NextStep();
      }
      else {
        vm.error = true;
      }
    };

    function checkOutputsSelected() {
      var outputsCount = 0;
      var outputsLength = vm.policy.outputs.length;

      for (var i = outputsLength-1; i>=0; i--) {
        if (vm.policy.outputs[i]) {
          outputsCount++;
        }
      }
      return outputsCount;
    };
  }
})();
