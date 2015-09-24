(function () {
    'use strict';

    angular
      .module('webApp')
      .factory('CubeModelFactory', CubeModelFactory);

    CubeModelFactory.$inject = [];

    function CubeModelFactory() {
      var cube = {};
      var error = {text: ""};

      function init(template) {
        cube.name = "";
        cube.dimensions = [];
        cube.operators = [];
        cube.checkpointConfig = {};
        cube.checkpointConfig.timeDimension = template.defaultTimeDimension;
        cube.checkpointConfig.interval = template.defaultInterval;
        cube.checkpointConfig.timeAvailability = template.defaultTimeAvailability;
        cube.checkpointConfig.granularity = template.defaultGranularity;
        error.text= "";
      }

      function resetCube(template) {
        init(template);
      }

      function getCube(template) {
        if (Object.keys(cube).length == 0) {
          init(template)
        }
        return cube;
      }

      function isValidCube() {
        var isValid = cube.name !== "" && cube.checkpointConfig.timeDimension !== "" && cube.checkpointConfig.interval !== null
          && cube.checkpointConfig.timeAvailability !== null && cube.checkpointConfig.granularity !== ""
          && cube.dimensions.length > 0 && cube.operators.length > 0;
        if (!isValid) {
          error.text = "_GENERIC_FORM_ERROR_";
        }
        return isValid;
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


