(function() {
'use strict';

/*POLICIES STEP CONTROLLER*/
angular
  .module('webApp')
  .controller('PolicyInputCtrl', PolicyInputCtrl);

PolicyInputCtrl.$inject = ['FragmentFactory','NewPoliceService', '$q'];

function PolicyInputCtrl(FragmentFactory,NewPoliceService, $q) {
  var vm = this;
  vm.policy = NewPoliceService.GetCurrentPolicy();
  vm.init = init;

  function init() {
    var defer = $q.defer();
    var inputList = FragmentFactory.GetFragments("input");

    inputList.then(function (result) {
      console.log(result);
      vm.inputList = result;

      defer.resolve();
    }, function () {
      defer.reject();
    });

    return defer.promise;
  }
};
})();
