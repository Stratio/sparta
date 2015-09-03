(function() {
'use strict';

/*POLICIES STEP CONTROLLER*/
angular
  .module('webApp')
  .controller('NewPolicyCtrl', NewPolicyCtrl);

NewPolicyCtrl.$inject = ['PolicyStaticDataService', '$q'];
function NewPolicyCtrl(PolicyStaticDataService,  $q) {
  var vm = this;

  vm.steps = PolicyStaticDataService.steps;

};
})();
