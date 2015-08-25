(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiPolicyService', ApiPolicyService);

    ApiPolicyService.$inject = ['$resource'];

    function ApiPolicyService($resource) {
        var vm = this;

        vm.GetPolicyByFragmentName = GetPolicyByFragmentName;
        vm.GetAllpolicies = GetAllpolicies;

        /////////////////////////////////

        function GetPolicyByFragmentName() {
            return $resource('/policy/fragment/:type/:name', {type:'@type', name:'@name'},
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