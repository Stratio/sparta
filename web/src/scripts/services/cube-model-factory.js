(function () {
    'use strict';

    angular
      .module('webApp')
      .factory('CubeModelFactory', CubeModelFactory);

    CubeModelFactory.$inject = [];

    function CubeModelFactory() {
      var cube = {};
      var order = 0;
      var error = {text: ""};

      function init(template, position) {
        if (position === undefined){
          position = order;
        };
        cube.name = template.defaultCubeName + position;
        cube.dimensions = [];
        cube.operators = [];
        cube.checkpointConfig = {};
        cube.checkpointConfig.timeDimension = template.defaultTimeDimension;
        cube.checkpointConfig.interval = template.defaultInterval;
        cube.checkpointConfig.timeAvailability = template.defaultTimeAvailability;
        cube.checkpointConfig.granularity = template.defaultGranularity;
        error.text= "";
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
        cube.dimensions =  c.dimensions;
        cube.operators =   c.operators;
        cube.checkpointConfig =  c.checkpointConfig
        cube.checkpointConfig =    c.checkpointConfig;
        error.text= "";
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
        setError: setError,
        getError: getError
      }
    }

  })
();


