(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('CubeCtrl', CubeCtrl);

  CubeCtrl.$inject = ['CubeModelFactory', 'PolicyModelFactory', '$modal'];

  function CubeCtrl(CubeModelFactory, PolicyModelFactory, $modal) {
    var vm = this;

    vm.init = init;
    vm.addOutputToDimensions = addOutputToDimensions;
    vm.removeOutputFromDimensions = removeOutputFromDimensions;
    vm.addFunctionToOperators = addFunctionToOperators;
    vm.removeFunctionFromOperators = removeFunctionFromOperators;

    vm.init();

    function init(cube) {
        vm.template =  PolicyModelFactory.getTemplate();
        if (cube) {
          vm.cube = cube;
        } else {
          vm.cube = CubeModelFactory.getCube(vm.template);
        }
        vm.policy = PolicyModelFactory.getCurrentPolicy();
        vm.granularityOptions = vm.template.granularityOptions;
        vm.functionList = vm.template.functionNames;
        vm.outputList = PolicyModelFactory.getAllModelOutputs();
        vm.cubeError = CubeModelFactory.getError();
    }

    function addOutputToDimensions(outputName) {
      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/dimension-modal.tpl.html',
        controller: 'NewDimensionModalCtrl as vm',
        size: 'lg',
        show: true,
        keyboard: false,
        backdrop: 'static',
        resolve: {
          fieldName: function () {
            return outputName;
          },
          dimensionName: function () {
            return outputName;
          },
          dimensions: function () {
            return vm.cube.dimensions
          },
          template: function(){
            return vm.template;
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
        show: true,
        keyboard: false,
        backdrop: 'static',
        resolve: {
          operatorType: function () {
            return functionName;
          },
          operatorName: function () {
            var operatorLength = vm.cube.operators.length + 1;
            return functionName.toLowerCase() + operatorLength;
          },
          operators: function () {
            return vm.cube.operators
          },
          template: function () {
            return  vm.template;
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
