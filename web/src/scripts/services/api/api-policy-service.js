(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiPolicyService', ApiPolicyService);

    ApiPolicyService.$inject = ['$resource'];

    function ApiPolicyService($resource) {
        var vm = this;

        vm.GetPolicyByFragmentId = GetPolicyByFragmentId;
        vm.GetAllpolicies = GetAllpolicies;

        /////////////////////////////////

        function GetPolicyByFragmentId() {
            return $resource('/policy/fragment/:type/:id', {type:'@type', id:'@id'},
            {
                'get': {method:'GET', isArray:true}
            });
        };

        function GetAllpolicies() {
            return $resource('/policy/all', {},
            {
                'get': {method:'GET', isArray:true}
            });
        };
    };
})();