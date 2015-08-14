(function() {
    'use strict';

    angular
        .module('webApp')
        .factory('FragmentDataService', ['ApiPolicyService', function(ApiPolicyService){

            return {
                GetPolicyByFragmentName: function(inputType, inputName) {
                    return ApiPolicyService.GetPolicyByFragmentName().get({'type': inputType ,'name': inputName}).$promise;
                }
            };

    }]);
})();
