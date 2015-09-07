(function() {
   'use strict';

   angular
       .module('webApp')
       .factory('FragmentFactory', FragmentFactory);

   FragmentFactory.$inject = ['ApiFragmentService'];

   function FragmentFactory(ApiFragmentService) {
       return {
               DeleteFragment: function(fragmentType, fragmentId) {
                   return ApiFragmentService.DeleteFragment().delete({'type': fragmentType ,'id': fragmentId}).$promise;
               },
               CreateFragment: function(newFragmentData) {
                   return ApiFragmentService.CreateFragment().create(newFragmentData).$promise;
               },
               UpdateFragment: function(updatedFragmentData) {
                   return ApiFragmentService.UpdateFragment().update(updatedFragmentData).$promise;
               },
               GetFragmentById: function(fragmentType, fragmentId) {
                  return ApiFragmentService.GetFragmentById().get({'type': fragmentType ,'id': fragmentId}).$promise;
               },
               GetFragments: function(fragmentType) {
                  return ApiFragmentService.GetFragments().get({'type': fragmentType}).$promise;
               },
               GetFakeFragments: function(fragmentType) {
                  return ApiFragmentService.GetFakeFragments().get({'type': fragmentType}).$promise;
               }
           };
   };
})();
