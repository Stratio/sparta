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

  /* Error handler directive */

  angular
    .module('webApp')
    .directive('cAnimatedMsg', cAnimatedMsg);

  cAnimatedMsg.$inject = ['$timeout'];


  function cAnimatedMsg($timeout) {
    var timer = null;

    var directive = {
      restrict: 'E',
      scope: {
        msg: "=",
        onClickCloseMsg: "&",
        timeout: "@"
      },
      replace: "true",
      templateUrl: 'templates/components/c-animated-msg.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      function startTimeout() {
        if (scope.timeout) {
          timer = $timeout(function () {
            scope.msg.text = '';
            scope.msg.internalTrace = '';
          }, scope.timeout);
        }
      }

      scope.$watchCollection(
        "msg",
        function (msg) {
          if (msg && msg.text != "") {
            startTimeout();
          }
        }
      );

      scope.$on(
        "$destroy",
        function (event) {
          $timeout.cancel(timer);
        }
      );
    }
  }
})();
