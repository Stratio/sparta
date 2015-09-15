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
      if (cube) {
        vm.cube = cube;
      } else {
        vm.cube = CubeModelFactory.getCube();
      }
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.granularityOptions = CubeStaticDataFactory.getGranularityOptions();
      vm.functionList = CubeStaticDataFactory.getFunctionNames();
      vm.outputList = PolicyModelFactory.getAllModelOutputs();
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
          },
          dimensionName: function () {
            var functionLength = vm.cube.dimensions.length + 1;
            return outputName + functionLength;
          },
          type: function () {
            return CubeStaticDataFactory.getDefaultType().value
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
          operatorType: function () {
            return functionName;
        },
          operatorName: function () {
            var operatorLength = vm.cube.operators.length + 1;
            return functionName.toLowerCase() + operatorLength;

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
