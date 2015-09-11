(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('CubeModelFactory', CubeModelFactory);

  function CubeModelFactory() {
    var cube = {};

    function init() {
      cube.name = "";
      cube.dimensions = [];
      cube.operators = [];
      cube.showCubeError = false;
      cube.checkpointConfig = {};
      cube.checkpointConfig.timeDimension = "";
      cube.checkpointConfig.interval = "";
      cube.checkpointConfig.timeAvailability = "";
      cube.checkpointConfig.granularity = "";
    };

    return {
      ResetCube: function () {
        init();
      },
      GetCube: function () {
        if (Object.keys(cube).length == 0) init();
        return cube;
      }
    }
  }

})
();


