(function() {
   'use strict';

   angular
       .module('webApp')
       .factory('FragmentDataService', FragmentDataService);

   FragmentDataService.$inject = ['ApiFragmentService'];

   function FragmentDataService(ApiFragmentService) {
       return {
               DeleteFragment: function(fragmentType, fragmentName) {
                   return ApiFragmentService.DeleteFragment().delete({'type': fragmentType ,'name': fragmentName}).$promise;
               },
               CreateFragment: function(newFragmentData) {
                   return ApiFragmentService.CreateFragment().create(newFragmentData).$promise;
               },
               UpdateFragment: function(updatedFragmentData) {
                   return ApiFragmentService.UpdateFragment().update(updatedFragmentData).$promise;
               },
               GetFragmentByName: function(fragmentType, fragmentName) {
                  return ApiFragmentService.GetFragmentByName().get({'type': fragmentType ,'name': fragmentName}).$promise;
               },
               GetFragments: function(fragmentType) {
                  return ApiFragmentService.GetFragments().get({'type': fragmentType}).$promise;
               }
           };
   };
})();
