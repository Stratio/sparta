(function () {
  'use strict';

  /*POLICY INPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyInputCtrl', PolicyInputCtrl);

  PolicyInputCtrl.$inject = ['FragmentFactory', 'PolicyModelFactory', '$q'];

  function PolicyInputCtrl(FragmentFactory, PolicyModelFactory, $q) {
    var vm = this;
    vm.setInput = setInput;
    vm.isSelectedInput = isSelectedInput;
    vm.previousStep = previousStep;
    vm.validateForm = validateForm;
    vm.inputList = [];
    vm.error = false;
    init();

    function init() {
      var defer = $q.defer();
      vm.template = PolicyModelFactory.getTemplate();
      vm.helpLink = vm.template.helpLinks.inputs;
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      var inputList = FragmentFactory.getFragments("input");
      inputList.then(function (result) {
        vm.inputList = result;
        defer.resolve();
      }, function () {
        defer.reject();
      });
      return defer.promise;
    };

    function setInput(index) {
      vm.policy.input = vm.inputList[index];
      vm.error = false
    };

    function isSelectedInput(name) {
      if (vm.policy.input)
        return name == vm.policy.input.name;
      else
        return false;
    };

    function previousStep() {
      PolicyModelFactory.previousStep();
    }

    function validateForm() {
      if (vm.policy.input.name) {
        vm.error = false;
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = true;
      }
    }
  };
})();
