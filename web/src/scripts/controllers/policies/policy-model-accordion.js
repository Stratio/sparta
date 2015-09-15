(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelAccordionCtrl', PolicyModelAccordionCtrl);

  PolicyModelAccordionCtrl.$inject = ['PolicyModelFactory', 'ModelStaticDataFactory', 'AccordionStatusService',
    'ModelFactory', 'PolicyStaticDataFactory', 'CubeService', '$modal'];

  function PolicyModelAccordionCtrl(PolicyModelFactory, ModelStaticDataFactory, AccordionStatusService,
                                    ModelFactory, PolicyStaticDataFactory, CubeService, $modal) {
    var vm = this;
    var index = 0;

    vm.init = init;
    vm.addModel = addModel;
    vm.removeModel = removeModel;
    vm.nextStep = nextStep;
    vm.getIndex = getIndex;
    vm.isLastModel = isLastModel;
    vm.modelError = false;

    vm.init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.newModel = ModelFactory.getModel();
      vm.accordionStatus = AccordionStatusService.accordionStatus;
      vm.templateModelData = ModelStaticDataFactory;
      AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
      vm.helpLink = PolicyStaticDataFactory.helpLinks.models;
    }

    function addModel() {
      vm.error = false;
      if (ModelFactory.isValidModel()) {
        vm.modelError = false;
        var newModel = angular.copy(vm.newModel);
        newModel.order = vm.policy.models.length + 1;
        vm.policy.models.push(newModel);
        ModelFactory.resetModel();
        AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
        AccordionStatusService.accordionStatus.newItem = true;
      } else
        vm.error = true;
    }

    function removeModel(index) {

      //check if there are cubes whose dimensions have fields == model.outputFields
      var cubeNames = CubeService.findCubesUsingOutputs(vm.policy.cubes, vm.policy.models[index].outputFields);
      console.log(cubeNames);
      //TODO show an alert to confirm to delete the cubes contained in cubeNmaes
      showConfirmRemoveModel();
      //TODO If alert is confirmed by user, remove cubes and model
      //vm.policy.models.splice(index, 1);
      //vm.newModelIndex = vm.policy.models.length;
      //AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
      //AccordionStatusService.getAccordionStatus().newItem = true;
      //ModelFactory.resetModel();
    }

    function showConfirmRemoveModel()
    {
      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/st-confirm-policy-modal.tpl.html',
        controller: 'ConfirmPolicyModalCtrl as vm',
        size: 'lg'
      });

      modalInstance.result.then(function (dimension) {
        vm.cube.dimensions.push(dimension);
      }, function () {
      });
    }
    function getIndex() {
      return index++;
    }

    function isLastModel(index) {
      return index == vm.policy.models.length - 1;
    }

    function nextStep() {
      if (vm.policy.models.length > 0) {
        vm.modelError = false;
        PolicyModelFactory.nextStep();
      }
      else {
        vm.modelError = true;
      }
    }
  }
})();
