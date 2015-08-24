'use strict';

    angular.module('webApp')
    .service('API', ['$resource', function ApiDownloadModules($resource) {

        /*var url = '/policy/all';*/
        /*var url = '/template/inputs';*/
        var url = '/fragment/input';

        var methods = {
          'get'   : {method:'GET', isArray:true}
        };

        var resource =  $resource(url,{},methods);

        return resource;

/*
        this.getInputData = function() {
            var vm = th

            var url = '/policy/all';
            var url = '/template/inputs';
            var url = '/fragment/input';

            var methods = {
            'get'   : {method:'GET', isArray:true}
            };

            var resource =  $resource(url,{},methods);

            vm.resource = resource.query();

            vm.resource.$promise.then(function (result) {
                vm.inputsData = result;
                console.log(vm.inputsData);
                return vm.inputsData;
            });
        };
*/

      }]);