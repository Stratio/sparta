(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyCubeAccordionCtrl', PolicyCubeAccordionCtrl);

  PolicyCubeAccordionCtrl.$inject = ['PolicyModelFactory', 'CubeModelFactory', 'AccordionStatusService', 'CubeService', '$scope'];

  function PolicyCubeAccordionCtrl(PolicyModelFactory, CubeModelFactory, AccordionStatusService, CubeService, $scope) {
    var vm = this;
    var index = 0;

    vm.init = init;
    vm.previousStep = previousStep;
    vm.nextStep = nextStep;
    vm.generateIndex = generateIndex;
    vm.error = "";

    vm.init();

    function init() {
      CubeService.resetCreatedCubes();
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.accordionStatus = AccordionStatusService.getAccordionStatus();
      AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
      vm.helpLink = vm.template.helpLinks.cubes;
    }

    function generateIndex() {
      return index++;
    }

    function previousStep() {
      PolicyModelFactory.previousStep();
    }

    function nextStep() {
      if (vm.policy.cubes.length > 0 && CubeService.areValidCubes()) {
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = "_POLICY_._CUBE_ERROR_";
      }
    }

    $scope.$watchCollection(
      "vm.accordionStatus",
      function (newValue) {
        if (vm.accordionStatus) {
          var selectedCubePosition = newValue.indexOf(true);
            if (vm.policy.cubes.length > 0 && selectedCubePosition >= 0 && selectedCubePosition < vm.policy.cubes.length ) {
              var selectedCube = vm.policy.cubes[selectedCubePosition];
              CubeModelFactory.setCube(selectedCube,selectedCubePosition );
            } else {
              CubeModelFactory.resetCube(vm.template, CubeService.getCreatedCubes(), vm.policy.cubes.length);
            }
          }else{
        }
      }
    );
  }
})();
