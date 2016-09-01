/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function() {
   'use strict';

   angular
       .module('webApp')
       .factory('FragmentFactory', FragmentFactory);

   FragmentFactory.$inject = ['ApiFragmentService'];

   function FragmentFactory(ApiFragmentService) {
       return {
               deleteFragment: function(fragmentType, fragmentId) {
                   return ApiFragmentService.deleteFragment().delete({'type': fragmentType ,'fragmentId': fragmentId}).$promise;
               },
               createFragment: function(newFragmentData) {
                   return ApiFragmentService.createFragment().create(newFragmentData).$promise;
               },
               updateFragment: function(updatedFragmentData) {
                   return ApiFragmentService.updateFragment().update(updatedFragmentData).$promise;
               },
               getFragmentById: function(fragmentType, fragmentId) {
                  return ApiFragmentService.getFragmentById().get({'type': fragmentType ,'fragmentId': fragmentId}).$promise;
               },
               getFragments: function(fragmentType) {
                  return ApiFragmentService.getFragments().get({'type': fragmentType}).$promise;
               },
               getFakeFragments: function(fragmentType) {
                  return ApiFragmentService.getFakeFragments().get({'type': fragmentType}).$promise;
               }
           };
   }
})();
