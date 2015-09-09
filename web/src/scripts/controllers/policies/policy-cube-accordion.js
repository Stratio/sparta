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
      vm.cubes = CubeModelFactory.GetCubeList();
      vm.newCube = CubeModelFactory.GetNewCube();
      vm.accordionStatus = AccordionStatusService.accordionStatus;
      AccordionStatusService.ResetAccordionStatus(vm.cubes.length);
    }

    function addCube() {
      vm.cubes.push(angular.copy(vm.newCube));
      CubeModelFactory.ResetNewCube();
      AccordionStatusService.ResetAccordionStatus(vm.cubes.length);
    }

    function removeCube(index) {
      vm.cubes.splice(index, 1);
      AccordionStatusService.ResetAccordionStatus(vm.cubes.length);
      AccordionStatusService.accordionStatus.newItem = true;
    }

    function nextStep() {
      vm.policy.cubes = vm.cubes;
      PolicyModelFactory.NextStep();
    }


  }
})();
