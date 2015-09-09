(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyCubeAccordionCtrl', PolicyCubeAccordionCtrl);

  PolicyCubeAccordionCtrl.$inject = ['PolicyModelFactory', 'CubeModelFactory', 'AccordionStatusService'];

  function PolicyCubeAccordionCtrl(PolicyModelFactory, CubeModelFactory, AccordionStatusService) {
    var vm = this;
    vm.init = init;
    vm.nextStep = nextStep;
    vm.addCube = addCube;
    vm.removeCube = removeCube;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.policy.cubes = CubeModelFactory.GetCubeList();
      vm.newCube = CubeModelFactory.GetNewCube();
      vm.accordionStatus = AccordionStatusService.accordionStatus;
      AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length);
    }

    function addCube() {
      vm.policy.cubes.push(angular.copy(vm.newCube));
      CubeModelFactory.ResetNewCube();
      AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length);
    }

    function removeCube(index) {
      vm.policy.cubes.splice(index, 1);
      AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length);
      AccordionStatusService.accordionStatus.newItem = true;
    }

    function nextStep() {
      PolicyModelFactory.NextStep();
    }


  }
})();
