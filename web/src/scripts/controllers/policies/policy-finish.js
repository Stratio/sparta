(function () {
  'use strict';

  /*POLICY FINISH CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyFinishCtrl', PolicyFinishCtrl);

  PolicyFinishCtrl.$inject = ['PolicyModelFactory'];

  function PolicyFinishCtrl(PolicyModelFactory) {
    var vm = this;
    vm.previousStep = previousStep;

    init();

    ///////////////////////////////////////

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();

      var finalJSON = generateFinalJSON();
      PolicyModelFactory.setFinalJSON(finalJSON);
      vm.testingpolcyData = JSON.stringify(finalJSON, null, 4);
    }

    function previousStep() {
      PolicyModelFactory.previousStep();
    }

    function generateFinalJSON() {
      var fragments = [];
      var finalJSON = angular.copy(vm.policy);
      fragments.push(finalJSON.input);
      for (var i = 0; i < finalJSON.outputs.length; ++i) {
        if (finalJSON.outputs[i]) {
          fragments.push(finalJSON.outputs[i]);
        }
      }
      finalJSON.fragments = fragments;
      finalJSON = cleanPolicyJSON(finalJSON);

      return finalJSON;
    }

    function cleanPolicyJSON(finalJSON) {
      delete finalJSON.input;
      if (finalJSON.rawData.enabled === 'false') {
        delete finalJSON.rawData['path'];
        delete finalJSON.rawData['partitionFormat'];
      }
      return finalJSON;
    }
  }
})();
