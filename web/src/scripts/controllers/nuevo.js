(function() {
  'use strict';

  /**
   * @ngdoc function
   * @name webApp.controller:NuevoCtrl
   * @description
   * # NuevoCtrl
   * Controller of the webApp
   */
  angular.module('webApp')
    .controller('NuevoCtrl',['ApiTest', function (ApiTest) {

      this.init = function () {
        var vm = this;

        vm.name = 'Test';

        vm.test = ApiTest.get();
        console.log(vm.test);

/*
        ApiTest.get().then(function(response){
          console.log(response)
        });
*/
      };
    }]);

})();