(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyCubeAccordionCtrl', PolicyCubeAccordionCtrl);

  PolicyCubeAccordionCtrl.$inject = ['PolicyModelFactory', 'CubeModelFactory', 'AccordionStatusService', '$modal', '$q'];

  function PolicyCubeAccordionCtrl(PolicyModelFactory, CubeModelFactory, AccordionStatusService, $modal, $q) {
    var vm = this;
    var index = 0;
    var createdCubes = 0;

    vm.init = init;
    vm.previousStep = previousStep;
    vm.nextStep = nextStep;
    vm.addCube = addCube;
    vm.removeCube = removeCube;
    vm.getIndex = getIndex;
    vm.error = "";

    vm.init();

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.accordionStatus = AccordionStatusService.getAccordionStatus();
      createdCubes =  vm.policy.cubes.length;
      resetViewModel();

      vm.newCube = CubeModelFactory.getCube(vm.template, createdCubes+1);
      vm.helpLink = vm.template.helpLinks.cubes;
    }

    function resetViewModel() {
      CubeModelFactory.resetCube(vm.template, createdCubes+1);
      AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
    }

    function addCube() {
      if (CubeModelFactory.isValidCube(vm.policy.cubes)) {
        vm.error = "";
        vm.policy.cubes.push(angular.copy(vm.newCube));
        createdCubes++;
        resetViewModel();
      }
    }

    function removeCube(index) {
      var defer = $q.defer();
      showConfirmRemoveCube().then(function () {
        vm.policy.cubes.splice(index, 1);
        AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
        AccordionStatusService.accordionStatus.newItem = true;
        defer.resolve();
      }, function () {
        defer.reject()
      });
      return defer.promise;
    }

    function showConfirmRemoveCube() {
      var defer = $q.defer();

      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/modal/confirm-modal.tpl.html',
        controller: 'ConfirmModalCtrl as vm',
        size: 'lg',
        resolve: {
          title: function () {
            return "_REMOVE_CUBE_CONFIRM_TITLE_"
          },
          message: function () {
            return "";
          }
        }
      });

      modalInstance.result.then(function () {
        defer.resolve();
      }, function () {
        defer.reject();
      });
      return defer.promise;
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
