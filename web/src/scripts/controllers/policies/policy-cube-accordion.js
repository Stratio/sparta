(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyCubeAccordionCtrl', PolicyCubeAccordionCtrl);

  PolicyCubeAccordionCtrl.$inject = ['PolicyModelFactory', 'CubeModelFactory', 'CubeService','TriggerService', 'triggerConstants'];

  function PolicyCubeAccordionCtrl(PolicyModelFactory, CubeModelFactory, CubeService, TriggerService, triggerConstants) {
    var vm = this;

    vm.init = init;
    vm.changeOpenedCube = changeOpenedCube;
    vm.isActiveCubeCreationPanel = CubeService.isActiveCubeCreationPanel;
    vm.activateCubeCreationPanel = CubeService.activateCubeCreationPanel;
    vm.isActiveTriggerCreationPanel = TriggerService.isActiveTriggerCreationPanel;
    vm.activateTriggerCreationPanel = activateTriggerCreationPanel;

    vm.error = "";

    vm.init();

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      TriggerService.setTriggerContainer(vm.policy.cubeProcess.cubesTriggers, triggerConstants.CUBE);
      vm.cubeAccordionStatus = [];
      vm.triggerAccordionStatus = [];
      vm.helpLink = vm.template.helpLinks.cubes;
      if (vm.policy.cubes.length > 0) {
        PolicyModelFactory.enableNextStep();
      } else {
        CubeService.changeCubeCreationPanelVisibility(true);
      }
    }

    function activateCubeCreationPanel(){
     CubeService.activateCubeCreationPanel();
      TriggerService.disableTriggerCreationPanel();
    }

    function activateTriggerCreationPanel(){
      TriggerService.activateTriggerCreationPanel();
      CubeService.disableCubeCreationPanel();
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
