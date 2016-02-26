(function() {
   'use strict';

   angular
       .module('webApp')
       .factory('FragmentFactory', FragmentFactory);

   FragmentFactory.$inject = ['ApiFragmentService'];

   function FragmentFactory(ApiFragmentService) {
       return {
               deleteFragment: function(fragmentType, fragmentId) {
                   return ApiFragmentService.deleteFragment().delete({'type': fragmentType ,'id': fragmentId}).$promise;
               },
               createFragment: function(newFragmentData) {
                   return ApiFragmentService.createFragment().create(newFragmentData).$promise;
               },
               updateFragment: function(updatedFragmentData) {
                   return ApiFragmentService.updateFragment().update(updatedFragmentData).$promise;
               },
               getFragmentById: function(fragmentType, fragmentId) {
                  return ApiFragmentService.getFragmentById().get({'type': fragmentType ,'id': fragmentId}).$promise;
               },
               getFragments: function(fragmentType) {
                  return ApiFragmentService.getFragments().get({'type': fragmentType}).$promise;
               },
               getFakeFragments: function(fragmentType) {
                  return ApiFragmentService.getFakeFragments().get({'type': fragmentType}).$promise;
               }
           };
   };
})();
