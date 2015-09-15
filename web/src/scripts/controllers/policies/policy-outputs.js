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


    init();

    ////////////////////////////////////

    function init() {
      var defer = $q.defer();

      vm.helpLink = PolicyStaticDataFactory.helpLinks.outputs;
      vm.formSubmmited = false;
      vm.error = false;
      vm.outputList = [];
      vm.policy = PolicyModelFactory.getCurrentPolicy();

      var outputList = FragmentFactory.getFragments("output");
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
      var selectedOutputs = checkOutputsSelected();

      if (selectedOutputs > 0) {
        vm.error = false;
        PolicyModelFactory.nextStep();
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
