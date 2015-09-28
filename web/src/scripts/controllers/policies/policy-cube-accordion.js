(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyCubeAccordionCtrl', PolicyCubeAccordionCtrl);

  PolicyCubeAccordionCtrl.$inject = ['PolicyModelFactory', 'CubeModelFactory', 'AccordionStatusService', 'CubeService', 'ModalService', '$q'];

  function PolicyCubeAccordionCtrl(PolicyModelFactory, CubeModelFactory, AccordionStatusService, CubeService, ModalService, $q) {
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
      createdCubes = vm.policy.cubes.length;
      resetViewModel();

      vm.newCube = CubeModelFactory.getCube(vm.template, createdCubes + 1);
      vm.helpLink = vm.template.helpLinks.cubes;
    }

    function resetViewModel() {
      CubeModelFactory.resetCube(vm.template, createdCubes + 1);
      AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
    }

    function addCube() {
      if (CubeService.isValidCube(vm.newCube, vm.policy.cubes)) {
        vm.error = "";
        vm.policy.cubes.push(angular.copy(vm.newCube));
        createdCubes++;
        resetViewModel();
      } else {
        CubeModelFactory.setError("_GENERIC_FORM_ERROR_");
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
      var controller = "ConfirmModalCtrl";
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var title = "_REMOVE_CUBE_CONFIRM_TITLE_";
      var message = "";
      var resolve = {
        title: function () {
          return title
        }, message: function () {
          return message
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);

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
      if (vm.policy.cubes.length > 0 && CubeService.areValidCubes(vm.policy.cubes)) {
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = "_POLICY_._CUBE_ERROR_";
      }
    }
  }
})();
