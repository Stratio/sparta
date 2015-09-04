(function() {
'use strict';

/*POLICIES STEP CONTROLLER*/
angular
  .module('webApp')
  .controller('NewPolicyCtrl', NewPolicyCtrl);

NewPolicyCtrl.$inject = ['PolicyStaticDataFactory', '$q'];
function NewPolicyCtrl(PolicyStaticDataFactory) {
  var vm = this;

  vm.steps = PolicyStaticDataFactory.steps;
  vm.currentStep = 0;
  vm.continueToNextStep = continueToNextStep;

  function continueToNextStep(){
    vm.currentStep++;
  }
};
})();
