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
    vm.inputList = [];
    init();

    function init() {
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
    }

    function setInput(index) {
      vm.policy.input = vm.inputList[index];
    }

    function isSelectedInput(name) {
      if (vm.policy.input)
        return name == vm.policy.input.name;
      else
        return false;
    }
  };
})();
