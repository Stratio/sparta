(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyModelAccordionCtrl', PolicyModelAccordionCtrl);

  PolicyModelAccordionCtrl.$inject = ['PolicyModelFactory', 'AccordionStatusService',
    'ModelFactory', 'CubeService', 'ModalService', '$translate', '$q', '$scope'];

  function PolicyModelAccordionCtrl(PolicyModelFactory, AccordionStatusService,
                                    ModelFactory, CubeService, ModalService, $translate, $q, $scope) {
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
      //vm.newModel = ModelFactory.getModel(vm.template);
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

        showConfirmRemoveModel(cubeList.names).then(function () {
          removeCubes(cubeList.positions);
          vm.policy.models.splice(index, 1);
          vm.newModelIndex = vm.policy.models.length;
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

    function showConfirmRemoveModel(cubeNames) {
      var defer = $q.defer();
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var message = "";
      if (cubeNames.length > 0)
        message = $translate('_REMOVE_MODEL_MESSAGE_', {modelList: cubeNames.toString()});
      var resolve = {
        title: function () {
          return "_REMOVE_MODEL_CONFIRM_TITLE_"
        },
        message: function () {
          return message;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);

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
      function (newValue, oldValue) {
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
