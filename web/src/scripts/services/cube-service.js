(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeService', CubeService);

  function CubeService() {
    var vm = this;
    vm.findCubesUsingOutputs = findCubesUsingOutputs;

    function findCubesUsingOutputs(cubes, outputs) {
      var cubeNames = [];
      var currentCube = null;
      var found = false;
      for (var i = 0; i < cubes.length; ++i) {
        found = false;
        currentCube = cubes[i];
        found = findDimensionUsingOutputs(currentCube, outputs);
        if (found) {
          cubeNames.push(currentCube.name);
        }
      }
      return cubeNames;
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
  }
})();
