(function () {
  'use strict';

  angular
    .module('webApp')
    .service('OutputService', OutputService);

  OutputService.$inject = ['PolicyModelFactory', 'FragmentFactory',  '$q'];

  function OutputService(PolicyModelFactory,   FragmentFactory,  $q) {
    var vm = this;
    var outputList = null;

    vm.generateOutputList = generateOutputList;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();

    }

    function generateOutputList() {
      var defer = $q.defer();
      if (outputList) {
        defer.resolve(outputList);
      } else {
        outputList = [];
        FragmentFactory.getFragments("output").then(function (result) {
          for (var i = 0; i < result.length; ++i) {
            outputList.push({"label": result[i].name, "value": result[i].name});
          }
          defer.resolve(outputList);
        });
      }
      return defer.promise;
    }
  }
})();
