(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('CubeModelFactory', CubeModelFactory);

  function CubeModelFactory() {
    var cube = {};
    var cubes = [];

    function initNewCube() {
      cube.dimensions = [];
      cube.operators = [];
      cube.type = "";
      cube.configuration = "";
      cube.showCubeError = false;
    };

    return {
      GetCubeList: function () {
        return cubes;
      },
      ResetNewCube: function () {
        initNewCube();
      },
      GetNewCube: function () {
        if (Object.keys(cube).length == 0) initNewCube();
        return cube;
      }
    }
  }

})
();


