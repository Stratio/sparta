(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiPolicyService', ApiPolicyService);

    ApiPolicyService.$inject = ['$resource'];

    function ApiPolicyService($resource) {
        var vm = this;

        vm.GetPolicyByFragmentName = GetPolicyByFragmentName;
        vm.DeleteFragment = DeleteFragment;

        /////////////////////////////////

        function GetPolicyByFragmentName() {
            return $resource('/policy/fragment/:type/:name', {type:'@type', name:'@name'},
            {
                'get'   : {method:'GET', isArray:true}
            });
        };

        function DeleteFragment() {
            return $resource('/fragment/:type/:name', {type:'@type', name:'@name'},
            {
                'delete'   : {method:'DELETE'}
            });
        };
    };
})();