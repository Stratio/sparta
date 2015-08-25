(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiFragmentDataService', ApiFragmentDataService);

    ApiFragmentDataService.$inject = ['$resource'];

    function ApiFragmentDataService($resource) {
        var vm = this;

        vm.GetFragmentByName = GetFragmentByName;
        vm.GetFragments = GetFragments;

        /////////////////////////////////

        function GetFragmentByName() {
            return $resource('/fragment/:type/:name', {type:'@type', name:'@name'},
            {
                'get'   : {method:'GET'}
            });
        };

        function GetFragments() {
            return $resource('/fragment/:type', {type:'@type'},
            {
                'get'   : {method:'GET', isArray:true}
            });
        }
    }
})();