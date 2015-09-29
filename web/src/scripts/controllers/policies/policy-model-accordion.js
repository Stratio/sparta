(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelAccordionCtrl', PolicyModelAccordionCtrl);

  PolicyModelAccordionCtrl.$inject = ['PolicyModelFactory', 'AccordionStatusService',
    'ModelFactory', 'CubeService', 'ModelService','UtilsService', '$q', '$scope'];

  function PolicyModelAccordionCtrl(PolicyModelFactory, AccordionStatusService,
                                    ModelFactory, CubeService, ModelService, UtilsService, $q, $scope) {
    var vm = this;
    var index = 0;

    vm.init = init;
    vm.addModel = addModel;
    vm.removeModel = removeModel;
    vm.previousStep = previousStep;
    vm.nextStep = nextStep;
    vm.generateIndex = generateIndex;
    vm.isLastModel = isLastModel;

    vm.init();

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      ModelFactory.resetModel(vm.template);
      vm.accordionStatus = AccordionStatusService.getAccordionStatus();
      AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
      vm.helpLink = vm.template.helpLinks.models;
      vm.error = "";
    }

    function addModel() {
      vm.error = "";
      var modeToAdd = angular.copy(ModelFactory.getModel());
      if (ModelFactory.isValidModel()) {
        modeToAdd.order = vm.policy.models.length + 1;
        vm.policy.models.push(modeToAdd);
        AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
      }
    }

    function removeModel(index) {
      var defer = $q.defer();
      if (index !== undefined && index !== null && index >= 0 && index < vm.policy.models.length) {
        //check if there are cubes whose dimensions have model outputFields as fields
        var cubeList = CubeService.findCubesUsingOutputs(vm.policy.cubes, vm.policy.models[index].outputFields);

        ModelService.showConfirmRemoveModel(cubeList.names).then(function () {
          vm.policy.cubes = UtilsService.removeItemsFromArray(vm.policy.cubes, cubeList.positions);
          vm.policy.models.splice(index, 1);
          AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
          ModelFactory.resetModel(vm.template);
          defer.resolve();
        }, function () {
          defer.reject()
        });
      } else {
        defer.reject();
      }
      return defer.promise;
    }

    function generateIndex() {
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
        vm.error = "";
        PolicyModelFactory.nextStep();
      }
      else {
        vm.error = "_POLICY_._MODEL_ERROR_";
      }
    }

    $scope.$watchCollection(
      "vm.accordionStatus",
      function (newValue) {
        if (vm.accordionStatus) {
          var selectedModelPosition = newValue.indexOf(true);

            if (selectedModelPosition >= 0 && selectedModelPosition < vm.policy.models.length) {
              var selectedModel = vm.policy.models[selectedModelPosition];
              ModelFactory.setModel(selectedModel);
            }else{
              ModelFactory.resetModel(vm.template);
            }
        }
      }
    );
  }
})();
