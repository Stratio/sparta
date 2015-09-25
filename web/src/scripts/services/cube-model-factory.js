(function () {
    'use strict';

    angular
      .module('webApp')
      .factory('CubeModelFactory', CubeModelFactory);

    CubeModelFactory.$inject = ['UtilsService'];

    function CubeModelFactory(UtilsService) {
      var cube = {};
      var error = {text: ""};

      function init(template, position) {
        cube.name = template.defaultCubeName + position;
        cube.dimensions = [];
        cube.operators = [];
        cube.checkpointConfig = {};
        cube.checkpointConfig.timeDimension = template.defaultTimeDimension;
        cube.checkpointConfig.interval = template.defaultInterval;
        cube.checkpointConfig.timeAvailability = template.defaultTimeAvailability;
        cube.checkpointConfig.granularity = template.defaultGranularity;
        error.text= "";
      }

      function resetCube(template, position) {
        init(template, position);
      }

      function getCube(template, position) {
        if (Object.keys(cube).length == 0) {
          init(template, position)
        }
        return cube;
      }

      function isValidCube(cubes) {
        var isValid = cube.name !== "" && cube.checkpointConfig.timeDimension !== "" && cube.checkpointConfig.interval !== null
          && cube.checkpointConfig.timeAvailability !== null && cube.checkpointConfig.granularity !== ""
          && cube.dimensions.length > 0 && cube.operators.length > 0 && !nameExists(cubes);
        if (!isValid) {
          error.text = "_GENERIC_FORM_ERROR_";
        }
        return isValid;
      }

      function nameExists(cubes){
        var found = UtilsService.findElementInJSONArray(cubes, cube, "name");
        return found !== -1;
      }

      function getError() {
        return error;
      }
      return {
        resetCube: resetCube,
        getCube: getCube,
        isValidCube: isValidCube,
        getError: getError
      }
    }

  })
();


