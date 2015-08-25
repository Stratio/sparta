(function() {
   'use strict';

   angular
       .module('webApp')
       .factory('FragmentDataService', FragmentDataService);

   FragmentDataService.$inject = ['ApiPolicyService', 'ApiFragmentDataService'];

   function FragmentDataService(ApiPolicyService, ApiFragmentDataService) {
       return {
               GetPolicyByFragmentName: function(fragmentType, fragmentName) {
                   return ApiPolicyService.GetPolicyByFragmentName().get({'type': fragmentType ,'name': fragmentName}).$promise;
               },
               DeleteFragment: function(fragmentType, fragmentName) {
                   return ApiPolicyService.DeleteFragment().delete({'type': fragmentType ,'name': fragmentName}).$promise;
               },
               InsertFragment: function(newFragmentData) {
                   return ApiPolicyService.CreateFragment().create(newFragmentData).$promise;
               },
               UpdateFragment: function(updatedFragmentData) {
                   return ApiPolicyService.UpdateFragment().update(updatedFragmentData).$promise;
               },
               GetFragmentByName: function(fragmentType, fragmentName) {
                  return ApiFragmentDataService.GetFragmentByName().get({'type': fragmentType ,'name': fragmentName}).$promise;
               },
               GetFragments: function(fragmentType) {
                  return ApiFragmentDataService.GetFragments().get({'type': fragmentType}).$promise;
               }
           };
   };
})();
