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
               DeletePolicy: function(policyid) {
                  return ApiPolicyService.DeletePolicy().delete({'id': policyid}).$promise;
               },
               RunPolicy: function(policyid) {
                  return ApiPolicyService.RunPolicy().get({'id': policyid}).$promise;
               },
               StopPolicy: function(policy) {
                  return ApiPolicyService.StopPolicy().update(policy).$promise;
               },
               GetFakePolicy: function() {
                  return ApiPolicyService.GetFakePolicy().get().$promise;
               },



               getPoliciesStatus: function() {
                  return ApiPolicyService.getPoliciesStatus().get().$promise;
               }
           };
   };
})();
