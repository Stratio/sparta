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
        vm.CreatePolicy = CreatePolicy;
        vm.DeletePolicy = DeletePolicy;
        vm.RunPolicy = RunPolicy;
        vm.GetFakePolicy = GetFakePolicy;

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

        function CreatePolicy() {
            return $resource('/policy', {},
            {
                'create': {method:'POST'}
            });
        };

        function DeletePolicy() {
            return $resource('/policy/:id', {id:'@id'},
            {
                'delete': {method:'DELETE'}
            });
        };

        function RunPolicy() {
            return $resource('/policy/run/:id', {id:'@id'},
            {
                'get': {method:'GET'}
            });
        };

        function GetFakePolicy() {
            return $resource('/data-templates/fake_data/create_policies.json', {},
            {
                'get': {method:'GET'}
            });
        };
    };
})();