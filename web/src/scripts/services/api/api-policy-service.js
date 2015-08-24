(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiPolicyService', ApiPolicyService);

    ApiPolicyService.$inject = ['$resource'];

    function ApiPolicyService($resource) {
        var vm = this;

        vm.GetPolicyByFragmentName = GetPolicyByFragmentName;
<<<<<<< HEAD
=======
        vm.DeleteFragment = DeleteFragment;
        vm.CreateFragment = CreateFragment;
        vm.UpdateFragment = UpdateFragment;
>>>>>>> refactor(file names): replace underscores by dashes

        /////////////////////////////////

        function GetPolicyByFragmentName() {
            return $resource('/policy/fragment/:type/:name', {type:'@type', name:'@name'},
            {
                'get': {method:'GET', isArray:true}
            });
        };
<<<<<<< HEAD
=======

        function DeleteFragment() {
            return $resource('/fragment/:type/:name', {type:'@type', name:'@name'},
            {
                'delete': {method:'DELETE'}
            });
        };

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
>>>>>>> refactor(file names): replace underscores by dashes
    };
})();