(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('CubeModelFactory', CubeModelFactory);

  CubeModelFactory.$inject = ['UtilsService'];

  function CubeModelFactory(UtilsService) {
    var cube = {};
    var order = 0;
    var error = {text: ""};
    var context = {"position": null};

    function init(template, position) {
      if (position === undefined) {
        position = order;
      }

      cube.name = template.defaultCubeName + position;
      cube.dimensions = [];
      cube.operators = [];
      cube.checkpointConfig = {};
      cube.checkpointConfig.timeDimension = template.defaultTimeDimension;
      cube.checkpointConfig.interval = template.defaultInterval;
      cube.checkpointConfig.timeAvailability = template.defaultTimeAvailability;
      cube.checkpointConfig.granularity = template.defaultGranularity;
      error.text = "";
      order = position;
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

    function setCube(c) {
      cube.name = c.name;
      cube.dimensions = c.dimensions;
      cube.operators = c.operators;
      cube.checkpointConfig = c.checkpointConfig;
      error.text = "";
    }

    function isValidCube(cube, cubes, position) {
      var isValid = cube.name !== ""  && cube.name !== undefined && cube.checkpointConfig.timeDimension !== "" && cube.checkpointConfig.interval !== null
        && cube.checkpointConfig.timeAvailability !== null && cube.checkpointConfig.granularity !== ""
        && cube.dimensions.length > 0 && cube.operators.length > 0 && !nameExists(cube,cubes, position);
      return isValid;
    }

    function nameExists(cube, cubes, cubePosition) {
      var position = UtilsService.findElementInJSONArray(cubes, cube, "name");
      return position !== -1 && (position != cubePosition);
    }

    function getContext() {
      return context;
    }

    function setPosition(p){
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
      getContext:getContext,
      setPosition:setPosition,
      isValidCube: isValidCube,
      setError: setError,
      getError: getError
    }
  }

})
();


