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

  /* Slider section directive */

  angular
    .module('webApp')
    .directive('cSliderSection', cSliderSection);

  cSliderSection.$inject = ['$window'];

  function cSliderSection($window) {
    var directive = {
      restrict: 'E',
      replace: "true",
      link: link
    };

    return directive;

    function link(scope, $element, $attributes) {
      scope.hiddenSectionId = $attributes.hiddenSectionId;
      scope.showHiddenSection = true;
      scope.showSliderSectionButton = true;
      scope.changeHiddenSectionVisibility = function () {
        scope.showHiddenSection = !scope.showHiddenSection;
      };

      $window.onscroll = function () {
        var hiddenSectionHeight = getHiddenSectionHeight(scope.hiddenSectionId);
        if ((window.scrollY > hiddenSectionHeight && scope.showHiddenSection) || (window.scrollY > 1 && !scope.showHiddenSection)) {
          scope.showSliderSectionButton = false;
        } else {
          scope.showSliderSectionButton = true;
        }
        scope.$apply();
      };
    }

    function getHiddenSectionHeight(hiddenSectionId) {
      var hiddenSectionHeight = 0;
      if (hiddenSectionId) {
        hiddenSectionHeight = $('#' + hiddenSectionId).height();
      }
      return hiddenSectionHeight;
    }
  }
})();
