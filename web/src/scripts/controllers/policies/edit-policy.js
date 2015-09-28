(function () {
  'use strict';

  /*POLICY EDITION CONTROLLER*/
  angular
    .module('webApp')
    .controller('EditPolicyCtrl', EditPolicyCtrl);

  EditPolicyCtrl.$inject = ['TemplateFactory', 'PolicyModelFactory', 'PolicyFactory', '$q', 'ModalService', '$state', '$stateParams'];
  function EditPolicyCtrl(TemplateFactory, PolicyModelFactory, PolicyFactory, $q, ModalService, $state, $stateParams) {
    var vm = this;

    vm.confirmPolicy = confirmPolicy;

    init();

    function init() {
      var defer = $q.defer();
      TemplateFactory.getPolicyTemplate().then(function (template) {
        PolicyModelFactory.setTemplate(template);
        var id = $stateParams.id;
        vm.steps = template.steps;
        vm.status = PolicyModelFactory.getProcessStatus();
        vm.successfullySentPolicy = false;
        vm.error = null;
        PolicyFactory.getPolicyById(id).then(
          function (policyJSON) {
            PolicyModelFactory.setPolicy(policyJSON);
            vm.policy = PolicyModelFactory.getCurrentPolicy();
            defer.resolve();
          }
          , function () {
            defer.reject();
          });
      });

      return defer.promise;
    }

    function confirmPolicy() {
      var defer = $q.defer();
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var resolve = {
        title: function () {
          return "_POLICY_._WINDOW_._EDIT_._TITLE_";
        },
        message: function () {
          return "";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);

      modalInstance.result.then(function () {
        var finalJSON = PolicyModelFactory.getFinalJSON();

        PolicyFactory.savePolicy(finalJSON).then(function () {
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
