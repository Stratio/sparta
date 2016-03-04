(function () {
  'use strict';

  angular
    .module('webApp')
    .service('OutputService', OutputService);

  OutputService.$inject = ['FragmentFactory',  '$q'];

  function OutputService(  FragmentFactory,  $q) {
    var vm = this;
    var outputNameList = null;
    var outputList = null;

    vm.generateOutputNameList = generateOutputNameList;
    vm.getOutputList = getOutputList;

    function generateOutputNameList() {
      var defer = $q.defer();
      if (outputNameList) {
        defer.resolve(outputNameList);
      } else {
        outputNameList = [];
        FragmentFactory.getFragments("output").then(function (result) {
          for (var i = 0; i < result.length; ++i) {
            outputNameList.push({"label": result[i].name, "value": result[i].name});
          }
          defer.resolve(outputNameList);
        });
      }
      return defer.promise;
    }


    function getOutputList(){
      var defer = $q.defer();
      if (outputList) {
        defer.resolve(outputList);
      } else {
        outputList = [];
        FragmentFactory.getFragments("output").then(function (result) {
          defer.resolve(result);
        });
      }
      return defer.promise;
    }
  }
})();
