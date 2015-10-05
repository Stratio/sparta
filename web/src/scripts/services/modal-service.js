(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ModalService', ModalService);

  ModalService.$inject = ['$modal'];

  function ModalService($modal) {
    var vm = this;
    vm.openModal = openModal;

    function openModal(controller, templateUrl, resolve) {

      var modalInstance = $modal.open({
        animation: true,
        templateUrl: templateUrl,
        controller: controller + ' as vm',
        size: 'lg',
        resolve: resolve
      });
      return modalInstance;
    }
  }

})();
