(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ModelService', ModelService);

  ModelService.$inject = ['ModalService', 'PolicyModelFactory', '$translate', 'ModelFactory', 'CubeService', 'AccordionStatusService', 'UtilsService'];

  function ModelService(ModalService, PolicyModelFactory, $translate, ModelFactory, CubeService, AccordionStatusService, UtilsService) {
    var vm = this;
    vm.showConfirmRemoveModel = showConfirmRemoveModel;
    vm.policy = PolicyModelFactory.getCurrentPolicy();
    vm.addModel = addModel;
    vm.removeModel = removeModel;
    vm.isLastModel = isLastModel;
    vm.isNewModel = isNewModel;

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

    function addModel() {
      vm.error = "";
      var modelToAdd = angular.copy(ModelFactory.getModel());
      console.log(modelToAdd);
      if (ModelFactory.isValidModel()) {
        modelToAdd.order = vm.policy.models.length + 1;
        vm.policy.models.push(modelToAdd);
        AccordionStatusService.resetAccordionStatus(vm.policy.models.length);
      }
    }

    function removeModel(index) {
      var defer = $q.defer();
      if (index !== undefined && index !== null && index >= 0 && index < vm.policy.models.length) {
        //check if there are cubes whose dimensions have model outputFields as fields
        var cubeList = CubeService.findCubesUsingOutputs(vm.policy.cubes, vm.policy.models[index].outputFields);

        showConfirmRemoveModel(cubeList.names).then(function () {
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

    function isLastModel(index) {
      return index == vm.policy.models.length - 1;
    }

    function isNewModel(index) {
      return index == vm.policy.models.length;
    }

  }
})();
