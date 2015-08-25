(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiFragmentService', ApiFragmentService);

    ApiFragmentService.$inject = ['$resource'];

    function ApiFragmentService($resource) {
        var vm = this;

        vm.GetFragmentByName = GetFragmentByName;
        vm.GetFragments = GetFragments;
        vm.DeleteFragment = DeleteFragment;
        vm.CreateFragment = CreateFragment;
        vm.UpdateFragment = UpdateFragment;

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

        function CreateFragment() {
            return $resource('/fragment/', {},
            {
                'create': {method:'POST'}
            });
        };

        function UpdateFragment() {
            return $resource('/fragment/', {},
            {
               'update': {method:'PUT'}
            });
        };        

        function DeleteFragment() {
            return $resource('/fragment/:type/:name', {type:'@type', name:'@name'},
            {
                'delete': {method:'DELETE'}
            });
        };
    }
})();