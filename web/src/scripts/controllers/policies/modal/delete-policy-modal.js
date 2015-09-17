(function () {
  'use strict';

  /*DELETE POLICIES MODAL CONTROLLER */
  angular
    .module('webApp')
    .controller('DeletePolicyModalCtrl', DeletePolicyModalCtrl);

  DeletePolicyModalCtrl.$inject = ['$modalInstance', 'item', 'PolicyFactory'];

  function DeletePolicyModalCtrl($modalInstance, item, PolicyFactory) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.error = false;

    init();

    ///////////////////////////////////////

    function init() {
      vm.policyData = item;
    };

    function ok() {
      var policyToDelete = PolicyFactory.deletePolicy(vm.policyData.id);

      policyToDelete.then(function () {
        $modalInstance.close(vm.policyData);

      }, function (error) {
        vm.error = true;
        vm.errorText = "_INPUT_ERROR_" + error.data.i18nCode + "_";
      });
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
