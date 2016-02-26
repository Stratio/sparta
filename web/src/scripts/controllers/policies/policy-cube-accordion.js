(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyCubeAccordionCtrl', PolicyCubeAccordionCtrl);

  PolicyCubeAccordionCtrl.$inject = ['PolicyModelFactory', 'CubeModelFactory', 'CubeService'];

  function PolicyCubeAccordionCtrl(PolicyModelFactory, CubeModelFactory, CubeService) {
    var vm = this;

    vm.init = init;
    vm.previousStep = previousStep;
    vm.nextStep = nextStep;
    vm.changeOpenedCube = changeOpenedCube;
    vm.isActiveCubeCreationPanel = CubeService.isActiveCubeCreationPanel;
    vm.activateCubeCreationPanel = CubeService.activateCubeCreationPanel;

    vm.error = "";

    vm.init();

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.cubeAccordionStatus = [];
      vm.helpLink = vm.template.helpLinks.cubes;
      //vm.setTriggerContainer(vm.cubes.cubesTriggers);
      if (vm.policy.cubes.length > 0) {
        PolicyModelFactory.enableNextStep();
      } else {
        CubeService.changeCubeCreationPanelVisibility(true);
      }
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

    function changeOpenedCube(selectedCubePosition) {
        if (vm.policy.cubes.length > 0 && selectedCubePosition >= 0 && selectedCubePosition < vm.policy.cubes.length) {
          var selectedCube = vm.policy.cubes[selectedCubePosition];
          CubeModelFactory.setCube(selectedCube, selectedCubePosition);
        } else {
          CubeModelFactory.resetCube(vm.template.cube, CubeService.getCreatedCubes(), vm.policy.cubes.length);
        }
    }

  }
})();
