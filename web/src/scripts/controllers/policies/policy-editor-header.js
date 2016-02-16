(function () {
  'use strict';

  /*POLICY EDITION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyEditorHeaderCtrl', PolicyEditorHeaderCtrl);

  PolicyEditorHeaderCtrl.$inject = [];
  function PolicyEditorHeaderCtrl() {
    var vm = this;

    init();

    function init() {
      /*TODO: SET POLICY NAME*/
      vm.policyName = 'TODO: policy name';
    }
  }
})();
