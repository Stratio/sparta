(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('CubeModelFactory', CubeModelFactory);

  CubeModelFactory.$inject = ['UtilsService'];

  function CubeModelFactory(UtilsService) {
    var cube = {};
    var error = {text: ""};
    var context = {"position": null};

    function init(template, position) {
      setPosition(position);
      cube.name = template.defaultCubeName + (position+1);
      cube.dimensions = [];
      cube.operators = [];
      cube.checkpointConfig = {};
      cube.checkpointConfig.timeDimension = template.defaultTimeDimension;
      cube.checkpointConfig.interval = template.defaultInterval;
      cube.checkpointConfig.timeAvailability = template.defaultTimeAvailability;
      cube.checkpointConfig.granularity = template.defaultGranularity;
      error.text = "";
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

    function setCube(c, position) {
      cube.name = c.name;
      cube.dimensions = c.dimensions;
      cube.operators = c.operators;
      cube.checkpointConfig = c.checkpointConfig;
      error.text = "";
      setPosition(position);
    }

    function isValidCube(cube, cubes, position) {
      var validName = cube.name !== undefined && cube.name !== "";
      var validOperatorsAndDimensions = cube.operators.length > 0 && cube.dimensions.length > 0;
      var validCheckpointConfig = Object.keys(cube.checkpointConfig).length > 0 && cube.checkpointConfig.granularity && cube.checkpointConfig.timeAvailability !== null && cube.checkpointConfig.interval !== null && cube.checkpointConfig.timeDimension !== "";
      var isValid = validName && validOperatorsAndDimensions && validCheckpointConfig && !nameExists(cube, cubes, position);
      return isValid;
    }

    function nameExists(cube, cubes, cubePosition) {
      var position = UtilsService.findElementInJSONArray(cubes, cube, "name");
      return position !== -1 && (position != cubePosition);
    }

    function getContext() {
      return context;
    }

    function setPosition(p) {
      if (p === undefined) {
        p = 0;
      }
      context.position = p;
    }

    function getError() {
      return error;
    }

    function setError(err) {
      error.text = err;
    }

    return {
      resetCube: resetCube,
      getCube: getCube,
      setCube: setCube,
      getContext: getContext,
      setPosition: setPosition,
      isValidCube: isValidCube,
      setError: setError,
      getError: getError
    }
  }

})
();


