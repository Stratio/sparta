(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('CubeCtrl', CubeCtrl);

  CubeCtrl.$inject = ['CubeStaticDataFactory', 'CubeModelFactory', 'PolicyModelFactory', '$modal'];

  function CubeCtrl(CubeStaticDataFactory, CubeModelFactory, PolicyModelFactory, $modal) {
    var vm = this;

    vm.init = init;
    vm.addOutputToDimensions = addOutputToDimensions;
    vm.removeOutputFromDimensions = removeOutputFromDimensions;
    vm.addFunctionToOperators = addFunctionToOperators;
    vm.removeFunctionFromOperators = removeFunctionFromOperators;

    vm.init();

    function init(cube) {
      if (cube)
        vm.cube = cube;
      else vm.cube = CubeModelFactory.GetNewCube();
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      var models = vm.policy.models;
      if (models.length > 0) {
        vm.templateCubeData = CubeStaticDataFactory;
        vm.outputList = models[models.length - 1].outputs;
        vm.functionList = CubeStaticDataFactory.GetFunctions();
      }
    }

    function isValidCube() {
      return true; //TODO
    }

    function addOutputToDimensions(outputName) {
      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/dimension-modal.tpl.html',
        controller: 'NewDimensionModalCtrl as vm',
        size: 'lg',
        resolve: {
          fieldName: function () {
            return outputName;
          }
        }
      });
      modalInstance.result.then(function (dimension) {
        vm.cube.dimensions.push(dimension);
      }, function () {
      });
    }

    function removeOutputFromDimensions(dimensionIndex) {
      vm.cube.dimensions.splice(dimensionIndex, 1);
    }

    function addFunctionToOperators(functionName) {
      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/operator-modal.tpl.html',
        controller: 'NewOperatorModalCtrl as vm',
        size: 'lg',
        resolve: {
          functionName: function () {
            return functionName;
          }
        }
      });
      modalInstance.result.then(function (operator) {
        vm.cube.operators.push(operator);
      }, function () {
      });
    }

    function removeFunctionFromOperators(operatorIndex) {
      vm.cube.operators.splice(operatorIndex, 1);
    }
  }
})();
