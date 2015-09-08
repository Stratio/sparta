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
    vm.checkInput = checkInput;
    vm.inputList = [];
    vm.error = false;
    init();

    function init() {
      vm.helpLink = PolicyStaticDataFactory.helpLinks.inputs;

      var defer = $q.defer();
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      var inputList = FragmentFactory.GetFragments("input");
      inputList.then(function (result) {
        vm.inputList = result;
        defer.resolve();
      }, function () {
        defer.reject();
      });
      return defer.promise;
    };

    function setInput(index) {
      console.log(index);
      vm.policy.input = vm.inputList[index];
    };

    function isSelectedInput(name) {
      if (vm.policy.input)
        return name == vm.policy.input.name;
      else
        return false;
    };

    function checkInput() {
      var inputObjectLenght = Object.getOwnPropertyNames(vm.policy.input).length;
      if (inputObjectLenght > 0) {
        vm.error = false;
        PolicyModelFactory.NextStep();
      }
      else {
        vm.error = true;
      }
    };
  };
})();
