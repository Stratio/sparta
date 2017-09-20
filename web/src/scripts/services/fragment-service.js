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
      .service('FragmentService', FragmentService);

  FragmentService.$inject = ['fragmentConstants', '$filter'];

  function FragmentService(fragmentConstants, $filter) {
    var vm = this;
    vm.isValidFragmentName = isValidFragmentName;
  vm.getFragmentIcon = getFragmentIcon;
    
    function isValidFragmentName(fragment, fragmentTemplate) {
      var newFragmentName = fragment.name.toLowerCase();
      var fragmentNamesExisting = $filter('filter')(fragmentTemplate.fragmentNamesList, {'name': newFragmentName}, true);
      
      return  fragmentNamesExisting.length == 0;
    }
    
    function getFragmentIcon(fragmentType){
      return fragmentType !== fragmentConstants.CUSTOM ? 'icon-' + fragmentType.toLowerCase() : 'icon-puzzle';
    }
  }
})();
