(function() {
    'use strict';

    angular
        .module('webApp')
        .factory('FragmentDataService', FragmentDataService);

    FragmentDataService.$inject = ['ApiPolicyService'];

    function FragmentDataService(ApiPolicyService) {
        return {
            GetPolicyByFragmentName: function(fragmentType, fragmentName) {
                return ApiPolicyService.GetPolicyByFragmentName().get({'type': fragmentType ,'name': fragmentName}).$promise;
            },
            DeleteFragment: function(fragmentType, fragmentName) {
                return ApiPolicyService.DeleteFragment().delete({'type': fragmentType ,'name': fragmentName}).$promise;
            },
            InsertFragment: function(newFragmentData) {
                console.log('Factory');
                console.log(newFragmentData);

                return ApiPolicyService.CreateFragment().create(newFragmentData).$promise;
            }
        };
    };
})();
