(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeService', CubeService);

  CubeService.$inject = ['PolicyModelFactory', 'ModalService', 'AccordionStatusService', 'CubeModelFactory', '$q'];

  function CubeService(PolicyModelFactory, ModalService, AccordionStatusService, CubeModelFactory, $q) {
    var vm = this;
    var createdCubes = null;

    vm.findCubesUsingOutputs = findCubesUsingOutputs;
    vm.areValidCubes = areValidCubes;
    vm.showConfirmRemoveCube = showConfirmRemoveCube;
    vm.addCube = addCube;
    vm.saveCube = saveCube;
    vm.removeCube = removeCube;
    vm.isNewCube = isNewCube;
    vm.getCreatedCubes = getCreatedCubes;
    vm.resetCreatedCubes = resetCreatedCubes;
    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();

      createdCubes = vm.policy.cubes.length;
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

    function findCubesUsingOutputs(outputs) {
      var cubeNames = [];
      var cubePositions = [];
      var cubes = vm.policy.cubes;
      if (cubes && outputs) {
        var currentCube = null;
        var found = false;
        for (var i = 0; i < cubes.length; ++i) {
          found = false;
          currentCube = cubes[i];
          found = findDimensionUsingOutputs(currentCube, outputs);
          if (found) {
            cubeNames.push(currentCube.name);
            cubePositions.push(i);
          }
        }
      }
      return {names: cubeNames, positions: cubePositions};
    }

    function findDimensionUsingOutputs(cube, outputs) {
      var found = false;
      var currentDimension = null;
      var i = 0;
      while (!found && i < cube.dimensions.length) {
        currentDimension = cube.dimensions[i];
        if (outputs.indexOf(currentDimension.field) != -1) {
          found = true;
        } else {
          ++i;
        }
      }
      return found;
    }

    function areValidCubes() {
      var valid = true;
      var i = 0;
      var currentCube = null;
      if (vm.policy.cubes.length > 0) {
        while (valid && i < vm.policy.cubes.length) {
          currentCube = vm.policy.cubes[i];

          if (!CubeModelFactory.isValidCube(currentCube, vm.policy.cubes, i)) {
            valid = false;
          } else {
            ++i;
          }
        }
      } else {
        valid = false;
      }
      return valid;
    }


    function addCube() {
      var newCube = angular.copy(CubeModelFactory.getCube());
      if (CubeModelFactory.isValidCube(newCube, vm.policy.cubes, CubeModelFactory.getContext().position)) {
        vm.policy.cubes.push(newCube);
        createdCubes++;
        AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
      } else {
        CubeModelFactory.setError();
      }
    }

    function saveCube(cubeForm) {
      cubeForm.$submitted = true;
      var cube = angular.copy(CubeModelFactory.getCube());
      if (CubeModelFactory.isValidCube(cube, vm.policy.cubes, CubeModelFactory.getContext().position)) {
        cubeForm.$submitted = false;
        vm.policy.cubes[CubeModelFactory.getContext().position] = cube;
      } else {
        CubeModelFactory.setError();
      }
    }

    function removeCube() {
      var defer = $q.defer();
      var cubePosition = CubeModelFactory.getContext().position;
      showConfirmRemoveCube().then(function () {
        vm.policy.cubes.splice(cubePosition, 1);
        AccordionStatusService.resetAccordionStatus(vm.policy.cubes.length);
        defer.resolve();
      }, function () {
        defer.reject()
      });
      return defer.promise;
    }

    function isNewCube(index) {
      return index == vm.policy.cubes.length;
    }

    function getCreatedCubes() {
      return createdCubes;
    }

    function resetCreatedCubes() {
      createdCubes = vm.policy.cubes.length;
    }
  }
})();
