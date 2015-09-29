(function () {
    'use strict';

    angular
      .module('webApp')
      .factory('CubeModelFactory', CubeModelFactory);

    CubeModelFactory.$inject = [];

    function CubeModelFactory() {
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

      function getError() {
        return error;
      }

      function setError(err) {
        error.text = err;
      }
      return {
        resetCube: resetCube,
        getCube: getCube,
        setError: setError,
        getError: getError
      }
    }

  })
();


