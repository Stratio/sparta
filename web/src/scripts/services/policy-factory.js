(function() {
   'use strict';

   angular
       .module('webApp')
       .factory('PolicyFactory', PolicyFactory);

   PolicyFactory.$inject = ['ApiPolicyService'];

   function PolicyFactory(ApiPolicyService) {
       return {
               GetPolicyByFragmentId: function(fragmentType, fragmentId) {
                  return ApiPolicyService.GetPolicyByFragmentId().get({'type': fragmentType ,'id': fragmentId}).$promise;
               },
               GetAllPolicies: function() {
                  return ApiPolicyService.GetAllpolicies().get().$promise;
               },
               CreatePolicy: function(newPolicyData) {
                  return ApiPolicyService.CreatePolicy().create(newPolicyData).$promise;
               },
               GetFakePolicy: function() {
                  return ApiPolicyService.GetFakePolicy().get().$promise;
               }
           };
   };
})();
