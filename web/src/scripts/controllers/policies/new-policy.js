'use strict';

/*POLICIES STEP CONTROLLER*/
angular
  .module('webApp')
  .controller('NewPolicyCtrl', NewPolicyCtrl);

NewPolicyCtrl.$inject = [];
function NewPolicyCtrl() {
  var vm = this;
  vm.currentStep = 3;
  vm.steps = [{name: "description", icon: "icon-tag_left"}, {name: "input", icon: "icon-import"},
    {name: "model", icon: "icon-content-left"}, {name: "cubes", icon: "icon-box"},
    {name: "outputs", icon: "icon-export"}, {name: "advanced", icon: "icon-star"}];
};
