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
'use strict';

/**
 * @ngdoc overview
 * @name webApp
 * @description
 * # webApp
 *
 * Main module of the application.
 */
(function(angular) {
    'use strict';

    fetchData();

    /**
    * @ngdoc function
    * @name fetchData
    * @description
    * Get config before bootstrap the application
    */
    function fetchData() {
        var initInjector = angular.injector(['ng']);
        var $http = initInjector.get('$http');
        var timeout = 10000, userName = "";           
        return $http.get("config").then(function(config) {
            timeout = config.data.timeout;
            userName = config.data.userName;
        }).finally(function(){
            angular.module('ng').constant('apiConfigSettings', {
                timeout: timeout,
                userName: userName
            });
            bootstrapApplication();
        });
    }

  /**
   * @ngdoc function
   * @name bootstrapApplication
   * @description
   * Launch app bootstrap
   */
  function bootstrapApplication() {
    angular.element(document).ready(function () {
      angular.bootstrap(document, ['webApp']);
    });
  }
})(window.angular);