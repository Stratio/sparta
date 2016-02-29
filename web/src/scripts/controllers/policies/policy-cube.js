(function () {
  'use strict';

  /*POLICY CUBES CONTROLLER*/
  angular
    .module('webApp')
    .controller('CubeCtrl', CubeCtrl);

  CubeCtrl.$inject = ['CubeModelFactory', 'CubeService', 'OutputService', 'PolicyModelFactory', 'ModalService',
    'TriggerModelFactory', 'TriggerService', 'triggerConstants'];

  function CubeCtrl(CubeModelFactory, CubeService, OutputService, PolicyModelFactory, ModalService,
                    TriggerModelFactory, TriggerService, triggerConstants ) {
    var vm = this;

    vm.init = init;
    vm.addCube = addCube;
    vm.removeCube = CubeService.removeCube;
    vm.isNewCube = CubeService.isNewCube;
    vm.saveCube = CubeService.saveCube;
    vm.isActiveTriggerCreationPanel = TriggerService.isActiveTriggerCreationPanel;
    vm.activateTriggerCreationPanel = TriggerService.activateTriggerCreationPanel;

    vm.addOutputToDimensions = addOutputToDimensions;
    vm.removeOutputFromDimensions = removeOutputFromDimensions;
    vm.addFunctionToOperators = addFunctionToOperators;
    vm.removeFunctionFromOperators = removeFunctionFromOperators;
    vm.addOutput = addOutput;
    vm.changeOpenedTrigger = changeOpenedTrigger;
    vm.isTimeDimension = null;

    vm.init();

    function init() {
      vm.cube = CubeModelFactory.getCube();
      if (vm.cube) {
        vm.template = PolicyModelFactory.getTemplate().cube;
        vm.policy = PolicyModelFactory.getCurrentPolicy();
        vm.granularityOptions = vm.template.granularityOptions;
        vm.functionList = vm.template.functionNames;
        vm.outputList = PolicyModelFactory.getAllModelOutputs();
        vm.cubeError = CubeModelFactory.getError();
        vm.cubeContext = CubeModelFactory.getContext();

        initTriggerAccordion();

        return OutputService.generateOutputList().then(function (outputList) {
          vm.policyOutputList = outputList;
        });
      }
    }

    function initTriggerAccordion() {
      vm.selectedPolicyOutput = "";
      vm.triggerAccordionStatus = [];
      TriggerService.disableTriggerCreationPanel();
      TriggerService.setTriggerContainer(vm.cube.triggers, triggerConstants.CUBE);
      vm.triggerContainer = vm.cube.triggers;
    }

    function addOutputToDimensions(outputName) {
      var templateUrl = "templates/policies/dimension-modal.tpl.html";
      var controller = "NewDimensionModalCtrl";
      var extraClass = null;
      var size = 'lg';

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
        },
        isTimeDimension: function() {
          return vm.isTimeDimension;
        }
      };

      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, extraClass, size);

      return modalInstance.result.then(function (dimensionData) {
        vm.cube.dimensions.push(dimensionData.dimesion);
        vm.isTimeDimension = dimensionData.isTimeDimesion;
      });
    }

    function addFunctionToOperators(functionName) {
      var templateUrl = "templates/policies/operator-modal.tpl.html";
      var controller = "NewOperatorModalCtrl";
      var extraClass = null;
      var size = 'lg';
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
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, extraClass, size);

      return modalInstance.result.then(function (operator) {
        vm.cube.operators.push(operator);
      });
    }

    function showConfirmModal(title, message) {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var extraClass = null;
      var size = 'lg';
      var resolve = {
        title: function () {
          return title
        },
        message: function () {
          return message;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, extraClass, size);
      return modalInstance.result;
    }

    function removeOutputFromDimensions(dimensionIndex, computeLast) {
      var title = "_POLICY_._CUBE_._REMOVE_DIMENSION_CONFIRM_TITLE_";
      return showConfirmModal(title, "").then(function () {
        if (computeLast) {
          vm.isTimeDimension = false;
        }
        vm.cube.dimensions.splice(dimensionIndex, 1);

      })
    }

    function removeFunctionFromOperators(operatorIndex) {
      var title = "_POLICY_._CUBE_._REMOVE_OPERATOR_CONFIRM_TITLE_";
      return showConfirmModal(title, "").then(function () {
        vm.cube.operators.splice(operatorIndex, 1);
      });
    }

    function addCube() {
      vm.form.$submitted = true;
      vm.form.cubeOutputs.$invalid = (vm.cube.writer.outputs.length === 0)? true : false;
      if (vm.form.$valid && vm.cube.operators.length > 0 && vm.cube.dimensions.length > 0 && vm.cube.writer.outputs.length > 0) {
        vm.form.$submitted = false;
        CubeService.addCube();
        CubeService.changeCubeCreationPanelVisibility(false);
      }
      else {
        CubeModelFactory.setError();
        if (vm.cube.writer.outputs.length === 0) {
          document.querySelector('#cubeOutputs').focus();
        }
      }
    }

    function addOutput() {
      if (vm.selectedPolicyOutput && vm.cube.writer.outputs.indexOf(vm.selectedPolicyOutput) == -1) {
        vm.cube.writer.outputs.push(vm.selectedPolicyOutput);
      }
    }


    function changeOpenedTrigger(selectedTriggerPosition) {
      if (vm.cube.triggers.length > 0 && selectedTriggerPosition >= 0 && selectedTriggerPosition < vm.cube.triggers.length) {
        var selectedTrigger = vm.cube.triggers[selectedTriggerPosition];
        TriggerModelFactory.setTrigger(selectedTrigger, selectedTriggerPosition);
      } else {
        TriggerModelFactory.resetTrigger(vm.cube.triggers.length);
      }
    }
  }
})();
