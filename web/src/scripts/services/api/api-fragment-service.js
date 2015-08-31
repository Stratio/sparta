(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiFragmentService', ApiFragmentService);

    ApiFragmentService.$inject = ['$resource'];

    function ApiFragmentService($resource) {
        var vm = this;

        vm.GetFragmentById = GetFragmentById;
        vm.GetFragments = GetFragments;
        vm.DeleteFragment = DeleteFragment;
        vm.CreateFragment = CreateFragment;
        vm.UpdateFragment = UpdateFragment;

        /////////////////////////////////

        function GetFragmentById() {
            return $resource('/fragment/:type/:id', {type:'@type', id:'@id'},
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
            return $resource('/fragment/:type/:id', {type:'@type', id:'@id'},
            {
                'delete': {method:'DELETE'}
            });
        };
    }
})();