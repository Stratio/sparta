(function () {
  'use strict';

  /*POLICY EDITION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyEditorHeaderCtrl', PolicyEditorHeaderCtrl);

  PolicyEditorHeaderCtrl.$inject = ['TemplateFactory'];
  function PolicyEditorHeaderCtrl(TemplateFactory) {
    var vm = this;

    init();

    function init() {
      /*TODO: SET POLICY NAME*/
      vm.policyName = 'TODO: policy name';

      return TemplateFactory.getPolicyTemplate().then(function (template) {
        vm.helpLink = template.helpLinks.description;
      });
    }
  }
})();
