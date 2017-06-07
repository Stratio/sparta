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

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('LegalTermsModalCtrl', LegalTermsModalCtrl);

  LegalTermsModalCtrl.$inject = ['$uibModalInstance', 'EntityFactory'];

  function LegalTermsModalCtrl($uibModalInstance, EntityFactory) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.close = close;

    init();

    ///////////////////////////////////////

    function init(){
        EntityFactory.getAppInfo().then(function(appInfo){
            vm.appInfo = appInfo;
        });
    }

    function ok() {
      $uibModalInstance.close();
    }

    function close() {
      $uibModalInstance.dismiss('cancel');
    }
  }

})();
