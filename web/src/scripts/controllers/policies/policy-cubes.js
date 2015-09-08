(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyCubesCtrl', PolicyCubesCtrl);

  PolicyCubesCtrl.$inject = ['PolicyModelFactory', 'CubeStaticDataFactory', 'AccordionStatusService', '$modal'];

  function PolicyCubesCtrl(PolicyModelFactory, CubeStaticDataFactory, AccordionStatusService, $modal) {
    var vm = this;
    vm.init = init;
    vm.isCurrentCube = isCurrentCube;
    vm.getCubeInputs = getCubeInputs;
    vm.addCube = addCube;
    vm.getCurrentCube = getCurrentCube;
    vm.removeCube = removeCube;
    vm.nextStep = nextStep;
    vm.addOutputToDimensions = addOutputToDimensions;
    vm.removeOutputFromDimensions = removeOutputFromDimensions;
    vm.addFunctionToOperators = addFunctionToOperators;
    vm.removeFunctionFromOperators = removeFunctionFromOperators;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.accordionStatus = AccordionStatusService.GetAccordionStatus();
      vm.newCube = {};
      vm.newCubeIndex = vm.policy.cubes.length;
      vm.templateCubeData = CubeStaticDataFactory;

      initNewCube();
      AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length, vm.policy.cubes.length);

    }

    function isCurrentCube(index) {
      return (vm.newCubeIndex == index)
    }

    function getCubeInputs() {
      var cubes = vm.policy.cubes;
      var result = [];
      if (vm.newCubeIndex >= 0) {
        if (vm.newCubeIndex == 0)
          result = CubeStaticDataFactory.defaultInput;
        else {
          var cube = cubes[--vm.newCubeIndex];
          result = cube.inputs.concat(cube.outputs);
        }
      }
      return result;
    }

    function addCube() {
      if (isValidCube()) {
        var newCube = angular.copy(vm.newCube);
        vm.policy.cubes.push(newCube);
        initNewCube();
        AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length, vm.policy.cubes.length);
      } else
        vm.showCubeError = true;
    }

    function initNewCube() {
      vm.newCubeIndex = vm.policy.cubes.length;
      vm.newCube.inputs = vm.getCubeInputs();
      vm.newCube.outputs = [];
      vm.newCube.type = "";
      vm.newCube.configuration = "";
      vm.newCube.dimensionList = [];
      vm.showCubeError = false;

      initOutputs();
    }

    function initOutputs() {
      vm.outputList = [];
      if (vm.policy.models && vm.policy.models.length > 0) {
        var lastModel = vm.policy.models[vm.policy.models.length - 1];
        vm.outputList = lastModel.outputs;
      }
    }

    function getCurrentCube(index) {
      if (vm.policy.cubes.length == 0) {
        return vm.newCube;
      } else
        return vm.policy.cubes[index]
    }

    function removeCube(index) {
      vm.policy.cubes.splice(index, 1);
      vm.newCubeIndex = vm.policy.cubes.length;
      AccordionStatusService.ResetAccordionStatus(vm.policy.cubes.length, vm.policy.cubes.length);
    }

    function isValidCube() {
      return true; //TODO
    }

    function nextStep() {
      PolicyModelFactory.NextStep();
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
        vm.newCube.dimensionList.push(dimension);
      }, function () {
      });
    }

    function removeOutputFromDimensions(dimensionIndex) {
      vm.newCube.dimensionList.splice(dimensionIndex, 1);
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
        vm.newCube.operatorList.push(operator);
      }, function () {
      });
    }

    function removeFunctionFromOperators(operatorIndex){
      vm.newCube.dimensionList.splice(dimensionIndex, 1);
      }
  }
})();
