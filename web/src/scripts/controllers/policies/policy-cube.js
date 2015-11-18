(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('CubeCtrl', CubeCtrl);

  CubeCtrl.$inject = ['CubeModelFactory', 'CubeService', 'PolicyModelFactory', 'ModalService'];

  function CubeCtrl(CubeModelFactory, CubeService, PolicyModelFactory, ModalService) {
    var vm = this;

    vm.init = init;
    vm.addCube = addCube;
    vm.removeCube = CubeService.removeCube;
    vm.isNewCube = CubeService.isNewCube;
    vm.saveCube = CubeService.saveCube;
    vm.addOutputToDimensions = addOutputToDimensions;
    vm.removeOutputFromDimensions = removeOutputFromDimensions;
    vm.addFunctionToOperators = addFunctionToOperators;
    vm.removeFunctionFromOperators = removeFunctionFromOperators;

    vm.init();

    function init() {
      vm.cube = CubeModelFactory.getCube();
      if (vm.cube) {
        vm.template = PolicyModelFactory.getTemplate();
        vm.policy = PolicyModelFactory.getCurrentPolicy();
        vm.granularityOptions = vm.template.granularityOptions;
        vm.functionList = vm.template.functionNames;
        vm.outputList = PolicyModelFactory.getAllModelOutputs();
        vm.cubeError = CubeModelFactory.getError();
        vm.cubeContext = CubeModelFactory.getContext();
      }
    }

    function addOutputToDimensions(outputName) {
      var templateUrl = "templates/policies/dimension-modal.tpl.html";
      var controller = "NewDimensionModalCtrl";
      var resolve = {
        fieldName: function () {
          return outputName;
        },
        dimensionName: function () {
          return outputName;
        },
        dimensions: function () {
          return vm.cube.dimensions
        },
        template: function () {
          return vm.template;
        }
      };

      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);

      return modalInstance.result.then(function (dimension) {
        vm.cube.dimensions.push(dimension);
      });
    }

    function addFunctionToOperators(functionName) {
      var templateUrl = "templates/policies/operator-modal.tpl.html";
      var controller = "NewOperatorModalCtrl";
      var resolve = {
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
          return vm.template;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);

      return modalInstance.result.then(function (operator) {
        vm.cube.operators.push(operator);
      });
    }

    function showConfirmModal(title, message) {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var resolve = {
        title: function () {
          return title
        },
        message: function () {
          return message;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);
      return modalInstance.result;
    }

    function removeOutputFromDimensions(dimensionIndex) {
      var title = "_POLICY_._CUBE_._REMOVE_DIMENSION_CONFIRM_TITLE_";
      return showConfirmModal(title, "").then(function () {
        vm.cube.dimensions.splice(dimensionIndex, 1);
      })
    }

    function removeFunctionFromOperators(operatorIndex) {
      var title = "_POLICY_._CUBE_._REMOVE_OPERATOR_CONFIRM_TITLE_";
      return showConfirmModal(title, "").then(function () {
        vm.cube.operators.splice(operatorIndex, 1);
      });
    }

    function addCube(){
      vm.form.$submitted = true;
      if (vm.form.$valid && vm.cube.operators.length > 0 && vm.cube.dimensions.length > 0) {
        vm.form.$submitted = false;
        CubeService.addCube();
      }
      else {
        CubeModelFactory.setError();
      }
    }
  }
})();
