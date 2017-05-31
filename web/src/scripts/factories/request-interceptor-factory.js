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
    .factory('requestInterceptor', requestInterceptor);

  requestInterceptor.$inject = ['$q', '$rootScope', '$location', '$window', '$timeout'];

  function requestInterceptor($q, $rootScope, $location, $window, $timeout) {
    var timer;
    return {
      'responseError': function (rejection) {
        switch (rejection.status) {
          case 0:
          case 503:
            {
              $rootScope.error = "_ERROR_._UNAVAILABLE_SERVER_";       
              $timeout.cancel( timer );       
              timer = $timeout(function(){
                $rootScope.error = "";
              },8000);

              break;
            }
          case 401:
            {
              var host = $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/";
              $window.location.href = host;
              break;
            }
          case 403:
           {
            $rootScope.error = "_UNAUTHORIZED_MESSAGE_";
             $timeout.cancel( timer );       
              timer = $timeout(function(){
                $rootScope.error = "";
              },8000);
           }
          default:
            break;
        }
        return $q.reject(rejection);
      }
    }
  }
})();