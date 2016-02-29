(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ModelService', ModelService);

  ModelService.$inject = ['ModalService', 'PolicyModelFactory', '$translate', 'ModelFactory', 'CubeService', 'UtilsService', '$q'];

  function ModelService(ModalService, PolicyModelFactory, $translate, ModelFactory, CubeService, UtilsService, $q) {
    var vm = this;

    var showModelCreationPanel = null;

    vm.showConfirmRemoveModel = showConfirmRemoveModel;
    vm.addModel = addModel;
    vm.removeModel = removeModel;
    vm.isLastModel = isLastModel;
    vm.isNewModel = isNewModel;
    vm.changeModelCreationPanelVisibility = changeModelCreationPanelVisibility;
    vm.isActiveModelCreationPanel = isActiveModelCreationPanel;
    vm.activateModelCreationPanel = activateModelCreationPanel;
    vm.disableModelCreationPanel = disableModelCreationPanel;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      showModelCreationPanel = true;
    }


    function activateModelCreationPanel() {
      showModelCreationPanel = true;
    }

    function disableModelCreationPanel() {
      showModelCreationPanel = false;
    }

    function showConfirmRemoveModel(cubeNames) {
      var defer = $q.defer();
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var message = "";

      if (cubeNames && cubeNames.length > 0) {
        message = $translate.instant('_REMOVE_MODEL_MESSAGE_', {modelList: cubeNames.toString()});
      }
      var resolve = {
        title: function () {
          return "_REMOVE_MODEL_CONFIRM_TITLE_"
        },
        message: function () {
          return message;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);

      modalInstance.result.then(function () { //TODO Refactor
        defer.resolve();
      }, function () {
        defer.reject();
      });
      return defer.promise;
    }

    function addModel() {
      vm.error = "";
      var modelToAdd = angular.copy(ModelFactory.getModel());
      if (ModelFactory.isValidModel()) {
        console.log(modelToAdd)
        vm.policy.transformations.push(modelToAdd);
        PolicyModelFactory.enableNextStep();
      }
    }

    function removeModel() {
      var defer = $q.defer();
      var modelPosition = ModelFactory.getContext().position;
      //check if there are cubes whose dimensions have model outputFields as fields
      var cubeList = CubeService.findCubesUsingOutputs(vm.policy.transformations[modelPosition].outputFields);

      showConfirmRemoveModel(cubeList.names).then(function () {
        vm.policy.cubes = UtilsService.removeItemsFromArray(vm.policy.cubes, cubeList.positions);
        vm.policy.transformations.splice(modelPosition, 1);
        if (vm.policy.transformations.length == 0) {
          PolicyModelFactory.disableNextStep();
        }
        defer.resolve();
      }, function () {
        defer.reject()
      });
      return defer.promise;
    }

    function isLastModel(index) {
      return index == vm.policy.transformations.length - 1;
    }

    function isNewModel(index) {
      return index == vm.policy.transformations.length;
    }

    function changeModelCreationPanelVisibility(isVisible) {
      showModelCreationPanel = isVisible;
    }

    function isActiveModelCreationPanel() {
      return showModelCreationPanel;
    }
  }
})();
