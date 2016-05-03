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
    .directive('formFieldSelect', formFieldSelect);

  formFieldSelect.$inject = ['$document', '$timeout'];
  function formFieldSelect($document, $timeout) {
    var directive = {
      link: link,
      templateUrl: 'stratio-ui/template/form/form_field_select.html',
      restrict: 'AE',
      replace: true,
      scope: {
        field: '@',
        help: '@',
        label: '@',
        name: '@stName',
        placeholder: '@',
        trackBy: '@',
        selectClass: '@',
        autofocus: '=',
        defaultvalue: '=',
        form: '=',
        model: '=',
        options: '=',
        required: '=',
        disabled: '=',
        changeAction: '&',
        listCompressed: '=',
        hideMessage: '@',
        qa: '@',
        submittedForm: '=',
        outputsLength: '='
      }
    };
    return directive;

    function link(scope, element, attrs) {
      scope.help = "";
      scope.label = "";
      scope.name = "";
      scope.placeholder = "";
      scope.isFocused = false;
      scope.showHelp = false;

      if (scope.defaultvalue !== undefined && scope.model === undefined) {
          scope.model = scope.defaultvalue;
      }

      scope.$watch('autofocus', function(newValue, oldValue) {
        if (newValue) {
          var tags = element.find('input');
          if (tags.length>0) {
            tags[0].focus();
          }
        }
      });

      scope.changeActionHandler = function() {
        $timeout(function() {
          scope.changeAction();
        }, 0);
      };

      scope.keyupHandler = function($event) {
        if (scope.model) {
          $timeout(function() {
            angular.element($event.target).triggerHandler('change');
          }, 0, false);
        }
      };

      scope.toggleHelp = function(event) {
        if (scope.showHelp) {
          scope.showHelp = false;
          $document.unbind('click', externalClickHandler);
        } else {
          scope.showHelp = true;
          $document.bind('click', externalClickHandler);
        }
      };

      scope.focusOnInput = function() {
        $('#'+scope.name).focus();
      };

      scope.mouseLeaveTooltip = function() {
        var tooltipHolder = document.querySelector('#' + scope.name);
        if (tooltipHolder) {
          var tooltipParent = tooltipHolder.parentNode;
          if (tooltipParent.querySelector('.tooltip')) {
            tooltipParent.querySelector('.tooltip').addEventListener("mouseleave", function () {
              tooltipParent.removeChild(this);
            });
          }
        }
      };

      function externalClickHandler(event) {
        if (event.target.id == "help-"+scope.name)
          return;
        $document.unbind('click', externalClickHandler);
        scope.showHelp = false;
        scope.$apply();
      }
    }
  }
})();
