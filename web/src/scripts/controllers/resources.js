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
    .controller('ResourcesCtrl', ResourcesCtrl);

  ResourcesCtrl.$inject = ['$scope', '$state'];

  function ResourcesCtrl($scope, $state) {
    /*jshint validthis: true*/
    var vm = this;
    vm.selectedOption = $state.current.name.split("dashboard.resources.")[1].toUpperCase();
    vm.errorMessage = {
      type: 'error',
      text: '',
      internalTrace: ''
    };
    vm.successMessage = {
      type: 'success',
      text: '',
      internalTrace: ''
    };
    vm.menuOptions = [{
      text: '_MENU_DASHBOARD_PLUGINS_',
      isDisabled: false,
      name: 'PLUGINS'
    }, {
      text: '_MENU_DASHBOARD_DRIVERS_',
      isDisabled: false,
      name: 'DRIVERS'
    },{
      text: '_MENU_DASHBOARD_BACKUPS_',
      isDisabled: false,
      name: 'SETTINGS'
    }];

  }
})();
