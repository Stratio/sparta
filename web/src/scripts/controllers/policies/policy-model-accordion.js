(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelAccordionCtrl', PolicyModelAccordionCtrl);

  PolicyModelAccordionCtrl.$inject = ['PolicyModelFactory', 'ModelStaticDataFactory', 'AccordionStatusService',
    'ModelFactory', 'PolicyStaticDataFactory', 'CubeService', '$modal', '$translate', '$q'];

  function PolicyModelAccordionCtrl(PolicyModelFactory, ModelStaticDataFactory, AccordionStatusService,
                                    ModelFactory, PolicyStaticDataFactory, CubeService, $modal, $translate, $q) {
    var vm = this;
    var index = 0;

    vm.init = init;
    vm.addModel = addModel;
    vm.removeModel = removeModel;
    vm.previousStep = previousStep;
    vm.nextStep = nextStep;
    vm.getIndex = getIndex;
    vm.isLastModel = isLastModel;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      ModelFactory.resetModel();
      vm.newModel = ModelFactory.getModel();
      vm.accordionStatus = AccordionStatusService.accordionStatus;
      vm.templateModelData = ModelStaticDataFactory;
      AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
      vm.helpLink = PolicyStaticDataFactory.getHelpLinks().models;
      vm.error = "";
    }

    function addModel() {
      vm.error = "";
      if (ModelFactory.isValidModel()) {
        var newModel = angular.copy(vm.newModel);
        newModel.order = vm.policy.models.length + 1;
        vm.policy.models.push(newModel);
        ModelFactory.resetModel();
        AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
        AccordionStatusService.accordionStatus.newItem = true;
      }
    }

    function removeModel(index) {
      //check if there are cubes whose dimensions have fields == model.outputFields
      var cubeList = CubeService.findCubesUsingOutputs(vm.policy.cubes, vm.policy.models[index].outputFields);

      showConfirmRemoveModel(cubeList.names).then(function () {
        removeCubes(cubeList.positions);
        vm.policy.models.splice(index, 1);
        vm.newModelIndex = vm.policy.models.length;
        AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
        AccordionStatusService.getAccordionStatus().newItem = true;
        ModelFactory.resetModel();
      });
    }

    function showConfirmRemoveModel(cubeNames) {
      var defer = $q.defer();
      var message = "";
      if (cubeNames.length > 0)
        message = $translate('_REMOVE_MODEL_MESSAGE_', {modelList: cubeNames.toString()});

      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/confirm-modal.tpl.html',
        controller: 'ConfirmModalCtrl as vm',
        size: 'lg',
        resolve: {
          title: function () {
            return "_REMOVE_MODEL_CONFIRM_TITLE_"
          },
          message: function () {
            return message;
          }
        }
      });

      modalInstance.result.then(function () {
        defer.resolve();
      }, function () {
        defer.reject();
      });
      return defer.promise;
    }

    function removeCubes(cubePositions) {
      var cubePosition = null;
      for (var i = 0; i < cubePositions.length; ++i) {
        cubePosition = cubePositions[i];
        vm.policy.cubes.splice(cubePosition, 1);
      }
    }

    function getIndex() {
      return index++;
    }

    function isLastModel(index) {
      return index == vm.policy.models.length - 1;
    }

    function previousStep() {
      PolicyModelFactory.previousStep();
    }

    function nextStep() {
      if (vm.policy.models.length > 0) {
        vm.error = "" ;
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = "_POLICY_._MODEL_ERROR_" ;
      }
    }
  }
})();
