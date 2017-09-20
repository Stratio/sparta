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
(function () {
  'use strict';

  /* Accordion directive */

  angular
    .module('webApp')
    .directive('cSession', cSession);

  cSession.$inject = ['apiConfigSettings', '$document', '$window'];

  function cSession(apiConfigSettings, $document, $window) {

    var directive = {
      restrict: 'E',
      scope: {},
      templateUrl: 'templates/components/c-session.tpl.html',
      link: link
    };

    return directive;

    function link(scope, el, attr, ctrl) {
      scope.userName = apiConfigSettings.userName;
      scope.open = false;
      if (!scope.userName || !scope.userName.length) {
        return;
      }

      scope.openDropdown = function(){
        scope.open = !scope.open;
      }

      scope.logout =  function() {
        $window.location.href = 'logout';
      }

      $document.on('click', function (e) {
        if (el !== e.target && !el[0].contains(e.target)) {
          scope.$apply(function () {
            scope.open = false;
          });
        }
      });
    }
  }
})();
