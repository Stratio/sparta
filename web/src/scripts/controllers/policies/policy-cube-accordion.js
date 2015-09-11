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
    vm.nextStep = nextStep;
    vm.addCube = addCube;
    vm.removeCube = removeCube;
    vm.getIndex = getIndex;
    vm.error = false;
    vm.modelError = false;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.policy.cubes = [];
      vm.newCube = CubeModelFactory.GetCube();
      vm.accordionStatus = AccordionStatusService.accordionStatus;
      AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length);
      vm.helpLink = PolicyStaticDataFactory.helpLinks.cubes;
    };

    function addCube() {
      if (isValidCube()) {
        vm.modelError = false;
        vm.error = false;
        vm.policy.cubes.push(angular.copy(vm.newCube));
        CubeModelFactory.ResetCube();
        AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length);
      }
      else {
        vm.error = true;
      }
    };

    function removeCube(index) {
      vm.policy.cubes.splice(index, 1);
      AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length);
      AccordionStatusService.accordionStatus.newItem = true;
    };

    function isValidCube() {
      return vm.newCube.name !== "" && vm.newCube.checkpointConfig.timeDimension !== "" && vm.newCube.checkpointConfig.interval !== null && vm.newCube.checkpointConfig.timeAvailability !== null && vm.newCube.checkpointConfig.granularity !== "" &&  vm.newCube.dimensions.length > 0 && vm.newCube.operators.length > 0;
    };

    function getIndex() {
      return index++;
    };

    function nextStep() {
      if (vm.policy.cubes.length > 0) {
        PolicyModelFactory.NextStep();
      }
      else {
        vm.modelError = true;
      }
    };


  }
})();
