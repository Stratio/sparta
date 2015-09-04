(function() {
'use strict';

/*POLICIES STEP CONTROLLER*/
angular
  .module('webApp')
  .controller('NewPolicyCtrl', NewPolicyCtrl);

NewPolicyCtrl.$inject = ['PolicyStaticDataService', '$q'];
function NewPolicyCtrl(PolicyStaticDataService) {
  var vm = this;

  vm.steps = PolicyStaticDataService.steps;
  vm.currentStep = 0;
  vm.continueToNextStep = continueToNextStep;

  function continueToNextStep(){
    vm.currentStep++;
  }
};
})();
