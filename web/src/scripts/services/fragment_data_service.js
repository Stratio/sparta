(function() {
    'use strict';

    angular
        .module('webApp')
        .factory('FragmentDataService', FragmentDataService);

    FragmentDataService.$inject = ['ApiPolicyService'];

    function FragmentDataService(ApiPolicyService) {
        return {
                GetPolicyByFragmentName: function(inputType, inputName) {
                    return ApiPolicyService.GetPolicyByFragmentName().get({'type': inputType ,'name': inputName}).$promise;
                }
            };
    };
})();
