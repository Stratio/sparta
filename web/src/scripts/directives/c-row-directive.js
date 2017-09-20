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

  angular
    .module('webApp')
    .directive('cNewRow', cNewRow);

  cNewRow.$inject = [];

  function cNewRow() {
    return {
      restrict: 'A',
      link: function (scope, element, attrs) {
        var nr = attrs.cNewRow;
        if (nr && nr === 'true') {
          var emptyElement = angular.element('<div class="empty" style="width:100%"></div>');
          element.before(emptyElement);
        }
      }
    }
  }
})();