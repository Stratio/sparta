(function () {
  'use strict';

  /*POLICY CREATION CONTROLLER*/
  angular
    .module('webApp')
    .controller('NewPolicyCtrl', NewPolicyCtrl);

  NewPolicyCtrl.$inject = ['TemplateFactory', 'PolicyModelFactory', 'PolicyFactory', '$q', '$modal', '$state'];
  function NewPolicyCtrl(TemplateFactory, PolicyModelFactory, PolicyFactory, $q, $modal, $state) {
    var vm = this;

    vm.confirmPolicy = confirmPolicy;

    init();

    function init() {
      var defer = $q.defer();

      TemplateFactory.getPolicyTemplate().then(function(template) {
        PolicyModelFactory.setTemplate(template);
        vm.steps = template.steps;
        PolicyModelFactory.resetPolicy();
        vm.policy = PolicyModelFactory.getCurrentPolicy();
        vm.status = PolicyModelFactory.getProcessStatus();
        vm.successfullySentPolicy = false;
        vm.error = null;
        defer.resolve();
      });
      return defer.promise;
    }

    function confirmPolicy() {
      var defer = $q.defer();
      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/st-confirm-policy-modal.tpl.html',
        controller: 'ConfirmPolicyModalCtrl as vm',
        size: 'lg'
      });

      modalInstance.result.then(function () {
        var finalJSON = PolicyModelFactory.getFinalJSON();
        PolicyFactory.createPolicy(finalJSON).then(function () {
          PolicyModelFactory.resetPolicy();
          $state.go("dashboard.policies");

          defer.resolve();
        }, function (error) {
          if (error) {
            vm.error = error.data;
          }
          defer.reject();
        });

      }, function () {
        defer.resolve();
      });

      return defer.promise;
    };
  };
})();
