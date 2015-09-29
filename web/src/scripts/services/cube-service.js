(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeService', CubeService);

  CubeService.$inject = ['UtilsService', 'ModalService'];

  function CubeService(UtilsService, ModalService) {
    var vm = this;
    vm.findCubesUsingOutputs = findCubesUsingOutputs;
    vm.isValidCube = isValidCube;
    vm.areValidCubes = areValidCubes;
    vm.showConfirmRemoveCube = showConfirmRemoveCube;

    function findCubesUsingOutputs(cubes, outputs) {
      var cubeNames = [];
      var cubePositions = [];
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

    function isValidCube(cube, cubes, cubePosition) {
      return cube.name !== "" && cube.checkpointConfig.timeDimension !== "" && cube.checkpointConfig.interval !== null
        && cube.checkpointConfig.timeAvailability !== null && cube.checkpointConfig.granularity !== ""
        && cube.dimensions.length > 0 && cube.operators.length > 0 && !nameExists(cube, cubes, cubePosition);
    }

    function nameExists(cube, cubes, cubePosition) {
      var position = UtilsService.findElementInJSONArray(cubes, cube, "name");
      return position !== -1 && (position != cubePosition);
    }

    function areValidCubes(cubes) {
      var valid = true;
      var i = 0;
      var currentCube = null;
      if (cubes) {
        while (valid && i < cubes.length) {
          currentCube = cubes[i];
          if (!isValidCube(currentCube, cubes, i)) {
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
  }
})();
