(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('CubeModelFactory', CubeModelFactory);

  CubeModelFactory.$inject = ['UtilsService', 'PolicyModelFactory'];

  function CubeModelFactory(UtilsService, PolicyModelFactory) {
    var cube = {};
    var error = {text: ""};
    var context = {"position": null};

    function init(template, nameIndex, position) {
      setPosition(position);
      cube.name = template.defaultName + (nameIndex + 1);
      cube.dimensions = [];
      cube.operators = [];
      cube.checkpointConfig = {};
      cube.triggers = [];
      cube.writer = {
        outputs: []
      };
      error.text = "";
    }

    function resetCube(template, nameIndex, position) {
      init(template, nameIndex, position);
    }

    function getCube(template, nameIndex, position) {
      if (Object.keys(cube).length == 0) {
        init(template, nameIndex, position)
      }
      return cube;
    }

    function setCube(c, position) {
      cube.name = c.name;
      cube.dimensions = c.dimensions;
      cube.operators = c.operators;
      cube.checkpointConfig = c.checkpointConfig;
      cube.triggers = c.triggers;
      cube.writer = c.writer;
      error.text = "";

      setPosition(position);
    }

    function areValidOperatorsAndDimensions(cube) {
      var validOperatorsAndDimensionsLength = cube.operators.length > 0 && cube.dimensions.length > 0;
      var validFieldList = PolicyModelFactory.getAllModelOutputs();
      for (var i = 0; i < cube.dimensions.length; ++i) {
        if (validFieldList.indexOf(cube.dimensions[i].field) == -1)
          return false;
      }
      return validOperatorsAndDimensionsLength;
    }

    function isValidCube(cube, cubes, position) {
      var validName = cube.name !== undefined && cube.name !== "";
      /*var validCheckpointConfig = Object.keys(cube.checkpointConfig).length > 0 && cube.checkpointConfig.granularity &&
       cube.checkpointConfig.timeAvailability !== null && cube.checkpointConfig.interval !== null && cube.checkpointConfig.timeDimension !== "";*/
      /*var isValid = validName && areValidOperatorsAndDimensions(cube) && validCheckpointConfig && !nameExists(cube, cubes, position);*/
      var isValid = validName && areValidOperatorsAndDimensions(cube) && !nameExists(cube, cubes, position);
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

    function setError() {
      if (cube.operators.length === 0 && cube.dimensions.length === 0) {
        error.text = "_POLICY_CUBE_OPERATOR-DIMENSION_ERROR_";
      }
      else if (cube.operators.length === 0) {
        error.text = "_POLICY_CUBE_OPERATOR_ERROR_";
      }
      else {
        error.text = "_POLICY_CUBE_DIMENSION_ERROR_";
      }
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


