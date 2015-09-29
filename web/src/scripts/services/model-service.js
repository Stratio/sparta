(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ModelService', ModelService);

  ModelService.$inject = ['ModalService', '$translate'];

  function ModelService(ModalService, $translate) {
    var vm = this;
    vm.showConfirmRemoveModel = showConfirmRemoveModel;

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

  }
})();
