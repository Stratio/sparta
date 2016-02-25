(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeService', CubeService);

  CubeService.$inject = ['PolicyModelFactory', 'ModalService', 'CubeModelFactory', 'FragmentFactory', 'UtilsService', '$q'];

  function CubeService(PolicyModelFactory, ModalService, CubeModelFactory, FragmentFactory, UtilsService, $q) {
    var vm = this;
    var createdCubes, showCubeCreationPanel, outputList = null;

    vm.findCubesUsingOutputs = findCubesUsingOutputs;
    vm.areValidCubes = areValidCubes;
    vm.showConfirmRemoveCube = showConfirmRemoveCube;
    vm.addCube = addCube;
    vm.saveCube = saveCube;
    vm.removeCube = removeCube;
    vm.isNewCube = isNewCube;
    vm.getCreatedCubes = getCreatedCubes;
    vm.resetCreatedCubes = resetCreatedCubes;
    vm.changeCubeCreationPanelVisibility = changeCubeCreationPanelVisibility;
    vm.isActiveCubeCreationPanel = isActiveCubeCreationPanel;
    vm.generateOutputList = generateOutputList;
    vm.activateCubeCreationPanel = activateCubeCreationPanel;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      createdCubes = vm.policy.cubes.length;
      showCubeCreationPanel = true;
    }

    function activateCubeCreationPanel() {
      showCubeCreationPanel = true;
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
      newCube = UtilsService.convertDottedPropertiesToJson(newCube);
      if (CubeModelFactory.isValidCube(newCube, vm.policy.cubes, CubeModelFactory.getContext().position)) {
        vm.policy.cubes.push(newCube);
        createdCubes++;
        PolicyModelFactory.enableNextStep();
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
        if (vm.policy.cubes.length == 0) {
          PolicyModelFactory.disableNextStep();
        }
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

    function changeCubeCreationPanelVisibility(isVisible) {
      showCubeCreationPanel = isVisible;
    }

    function isActiveCubeCreationPanel() {
      return showCubeCreationPanel;
    }

    function resetCreatedCubes() {
      createdCubes = vm.policy.cubes.length;
    }

    function generateOutputList() {
      var defer = $q.defer();
      if (outputList) {
        defer.resolve(outputList);
      } else {
        outputList = [];
        FragmentFactory.getFragments("output").then(function (result) {
          for (var i = 0; i < result.length; ++i) {
            outputList.push({"label": result[i].name, "value": result[i].name});
          }
          defer.resolve(outputList);
        });
      }
      return defer.promise;
    }
  }
})();
