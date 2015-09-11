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
      cube.timeDimension = "";
      cube.interval = "";
      cube.timeAvailability = "";
      cube.granularity = "";
    };

    return {
      ResetNewCube: function () {
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


