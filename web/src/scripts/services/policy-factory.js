(function() {
   'use strict';

   angular
       .module('webApp')
       .factory('PolicyFactory', PolicyFactory);

   PolicyFactory.$inject = ['ApiPolicyService'];

   function PolicyFactory(ApiPolicyService) {
       return {
               GetPolicyByFragmentName: function(fragmentType, fragmentName) {
                   return ApiPolicyService.GetPolicyByFragmentName().get({'type': fragmentType ,'name': fragmentName}).$promise;
               },
               GetAllpolicies: function() {
                   return ApiPolicyService.GetAllpolicies().get().$promise;
               }
           };
   };
})();
