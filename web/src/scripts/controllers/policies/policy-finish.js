(function () {
  'use strict';

  /*POLICY INPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyFinishCtrl', PolicyFinishCtrl);

  PolicyFinishCtrl.$inject = ['PolicyModelFactory', '$modal'];

  function PolicyFinishCtrl(PolicyModelFactory, $modal) {
    var vm = this;

    init();

    ///////////////////////////////////////

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      var json = getFinalJSON();
      vm.testingpolcyData = JSON.stringify(json, null, 4);
    };

    function getFinalJSON(){
      var fragments = [];
      var policy = angular.copy(vm.policy);
      policy.transformations = policy.models;
      fragments.push(policy.input);
      fragments.concat(policy.outputs);
      policy.fragments = fragments;
      delete policy.models;
      delete policy.input;
      delete policy.outputs;

      return policy;
    }
  };
})();
