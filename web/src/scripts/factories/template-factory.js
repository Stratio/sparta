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
    .factory('TemplateFactory', TemplateFactory);

  TemplateFactory.$inject = ['$q', 'ApiTemplateService'];

  function TemplateFactory($q, ApiTemplateService) {
    return {
      getNewFragmentTemplate: function (fragmentType) {
        return ApiTemplateService.getFragmentTemplateByType().get({'type': fragmentType + '.json'}).$promise;
      },
      getPolicyTemplate: function () {
        return ApiTemplateService.getPolicyTemplate().get().$promise;
      },
      getDimensionTemplateByType: function(dimensionType) {
        var defer = $q.defer();
        dimensionType = dimensionType ? dimensionType.toLowerCase() : 'default';
        ApiTemplateService.getDimensionTemplateByType().get({'type': 'default.json'}).$promise.then(function(defaultTemplate){
          if (dimensionType !== 'default'){
              ApiTemplateService.getDimensionTemplateByType().get({'type': dimensionType + '.json'}).$promise.then(function(specificTemplate){
                defer.resolve({properties: defaultTemplate.properties.concat(specificTemplate.properties)});
              });
          }else {
            defer.resolve(defaultTemplate);
          }
        });
        return defer.promise;
      },
      getOperatorTemplateByType: function (operatorType) {
        operatorType = operatorType ? operatorType.toLowerCase(): '';
        operatorType = operatorType != 'count' ? 'default': operatorType;
        return ApiTemplateService.getOperatorTemplateByType().get({'type': operatorType + '.json'}).$promise;
      }
    };
  };
})();
