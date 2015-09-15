(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('CubeModelFactory', CubeModelFactory);

  CubeModelFactory.$inject = ['CubeStaticDataFactory'];

  function CubeModelFactory(CubeStaticDataFactory) {
    var cube = {};

    function init() {
      cube.name = "";
      cube.dimensions = [];
      cube.operators = [];
      cube.checkpointConfig = {};
      cube.checkpointConfig.timeDimension = "";
      cube.checkpointConfig.interval = CubeStaticDataFactory.getDefaultInterval();
      cube.checkpointConfig.timeAvailability = CubeStaticDataFactory.getDefaultTimeAvailability();
      cube.checkpointConfig.granularity = CubeStaticDataFactory.getDefaultGranularity();
    };

    function resetCube() {
      init();
    };

    function getCube() {
      if (Object.keys(cube).length == 0) {
        init()
      }
      return cube;
    }

    function isValidCube() {
      return cube.name !== "" && cube.checkpointConfig.timeDimension !== "" && cube.checkpointConfig.interval !== null
        && cube.checkpointConfig.timeAvailability !== null && cube.checkpointConfig.granularity !== ""
        && cube.dimensions.length > 0 && cube.operators.length > 0;
    };

    return {
      resetCube: resetCube,
      getCube: getCube,
      isValidCube: isValidCube
    }
  }

})
();


