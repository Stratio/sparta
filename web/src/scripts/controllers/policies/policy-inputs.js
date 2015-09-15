(function () {
  'use strict';

  /*POLICY INPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyInputCtrl', PolicyInputCtrl);

  PolicyInputCtrl.$inject = ['FragmentFactory', 'PolicyModelFactory', '$q', 'PolicyStaticDataFactory'];

  function PolicyInputCtrl(FragmentFactory, PolicyModelFactory, $q, PolicyStaticDataFactory) {
    var vm = this;
    vm.setInput = setInput;
    vm.isSelectedInput = isSelectedInput;
    vm.validateForm = validateForm;
    vm.inputList = [];
    vm.error = false;
    init();

    function init() {
      vm.helpLink = PolicyStaticDataFactory.helpLinks.inputs;

      var defer = $q.defer();
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

    function validateForm() {
      if (vm.policy.input.name){
        vm.error = false;
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = true;
      }
    }
  };
})();
