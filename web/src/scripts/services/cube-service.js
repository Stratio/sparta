(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeService', CubeService);

  CubeService.$inject = ['UtilsService'];

  function CubeService(UtilsService) {
    var vm = this;
    vm.findCubesUsingOutputs = findCubesUsingOutputs;
    vm.isValidCube = isValidCube;
    vm.areValidCubes = areValidCubes;

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
      var result = {names: cubeNames, positions: cubePositions};
      return result;
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
      var isValid = cube.name !== "" && cube.checkpointConfig.timeDimension !== "" && cube.checkpointConfig.interval !== null
        && cube.checkpointConfig.timeAvailability !== null && cube.checkpointConfig.granularity !== ""
        && cube.dimensions.length > 0 && cube.operators.length > 0 && !nameExists(cube, cubes, cubePosition);

      return isValid;
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
  }
})();
