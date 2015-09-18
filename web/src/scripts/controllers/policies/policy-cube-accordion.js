(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyCubeAccordionCtrl', PolicyCubeAccordionCtrl);

  PolicyCubeAccordionCtrl.$inject = ['PolicyModelFactory', 'CubeModelFactory', 'AccordionStatusService', 'PolicyStaticDataFactory'];

  function PolicyCubeAccordionCtrl(PolicyModelFactory, CubeModelFactory, AccordionStatusService, PolicyStaticDataFactory) {
    var vm = this;
    var index = 0;

    vm.init = init;
    vm.previousStep = previousStep;
    vm.nextStep = nextStep;
    vm.addCube = addCube;
    vm.removeCube = removeCube;
    vm.getIndex = getIndex;
    vm.error = false;
    vm.error = "";

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      CubeModelFactory.resetCube();
      vm.newCube = CubeModelFactory.getCube();
      vm.accordionStatus = AccordionStatusService.getAccordionStatus();
      AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
      vm.helpLink = PolicyStaticDataFactory.getHelpLinks().cubes;
    }

    function addCube() {
      if (CubeModelFactory.isValidCube()) {
        vm.error = "";
        vm.policy.cubes.push(angular.copy(vm.newCube));
        CubeModelFactory.resetCube();
        AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
      }
    }

    function removeCube(index) {
      vm.policy.cubes.splice(index, 1);
      AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
      AccordionStatusService.accordionStatus.newItem = true;
    }

    function getIndex() {
      return index++;
    }

    function previousStep() {
      PolicyModelFactory.previousStep();
    }

    function nextStep() {
      if (vm.policy.cubes.length > 0) {
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = "_POLICY_._CUBE_ERROR_";
      }
    }
  }
})();
