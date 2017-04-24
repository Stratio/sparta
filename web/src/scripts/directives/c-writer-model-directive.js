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

  /* Accordion directive */

  angular
      .module('webApp')
      .directive('cWriterModel', cWriterModel);

  cWriterModel.$inject = ['OutputService'];

  function cWriterModel(OutputService) {

    var directive = {
      restrict: 'E',
      scope: {
        model: "=",
        form: "=",
        template: "="
      },
      replace: true,
      templateUrl: 'templates/components/c-writer-model.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      if (!scope.model || !scope.model.writer) {
        scope.model.writer = {outputs: []};
      }

      OutputService.generateOutputNameList().then(function(outputList) {
        scope.policyOutputList = outputList;
      });
      
      scope.addOutput = function(){
        if (scope.selectedPolicyOutput && scope.model.writer.outputs.indexOf(scope.selectedPolicyOutput) == -1) {
          scope.model.writer.outputs.push(scope.selectedPolicyOutput);
        }
      }
      
    }
  }
})();
