(function() {
'use strict';

/*POLICIES STEP CONTROLLER*/
angular
  .module('webApp')
  .controller('NewPolicyCtrl', NewPolicyCtrl);

NewPolicyCtrl.$inject = ['PolicyStaticDataFactory', '$q', 'PolicyModelFactory'];
function NewPolicyCtrl(PolicyStaticDataFactory, $q, PolicyModelFactory) {
  var vm = this;

  vm.steps = PolicyStaticDataFactory.steps;
  vm.policy = PolicyModelFactory.GetCurrentPolicy();
};
})();
