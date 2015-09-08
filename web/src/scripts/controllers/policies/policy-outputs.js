(function () {
  'use strict';

  /*POLICY OUTPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyOutputCtrl', PolicyOutputCtrl);

  PolicyOutputCtrl.$inject = ['FragmentFactory', 'PolicyModelFactory', '$q', 'PolicyStaticDataFactory'];

  function PolicyOutputCtrl(FragmentFactory, PolicyModelFactory, $q, PolicyStaticDataFactory) {
    var vm = this;
    vm.outputList = [];
    init();

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
    }

  }
})();
