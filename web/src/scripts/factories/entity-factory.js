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
    .factory('EntityFactory', EntityFactory);

  EntityFactory.$inject = ['ApiEntitiesService', '$filter', '$q'];

  function EntityFactory(ApiEntitiesService, $filter, $q) {
    return {
      getAllPlugins: function () {
        return ApiEntitiesService.getAllPlugins().get().$promise;
      },
      deletePlugin: function (fileName) {
        return ApiEntitiesService.deletePlugin().delete({
          'fileName': fileName
        }).$promise;
      },
      createPlugin: function (file) {
        var fd = new FormData();
        fd.append('file', file);
        return ApiEntitiesService.createPlugin().put(fd).$promise;
      },
      getAllDrivers: function () {
        return ApiEntitiesService.getAllDrivers().get().$promise;
      },
      deleteDriver: function (fileName) {
        return ApiEntitiesService.deleteDriver().delete({
          'fileName': fileName
        }).$promise;
      },
      createDriver: function (file) {
        var fd = new FormData();
        fd.append('file', file);
        return ApiEntitiesService.createDriver().put(fd).$promise;
      },
      getAllBackups: function () {
        return ApiEntitiesService.getAllBackups().get().$promise;
      },
      buildBackup: function () {
        return ApiEntitiesService.buildBackup().get().$promise;
      },
      deleteBackup: function (fileName) {
        return ApiEntitiesService.deleteBackup().delete({
          'fileName': fileName
        }).$promise;
      },
      downloadBackup: function (fileName) {
        return ApiEntitiesService.downloadBackup().get({
          'fileName': fileName
        }).$promise;
      },
      uploadBackup: function (file) {
        var fd = new FormData();
        fd.append('file', file);
        return ApiEntitiesService.uploadBackup().put(fd).$promise;
      },
      deleteAllBackups: function () {
        return ApiEntitiesService.deleteAllBackups().delete({}).$promise;
      },
      executeBackup: function(fileName,deleteAllBefore ){
        return ApiEntitiesService.executeBackup().post({
          'fileName': fileName,
          'deleteAllBefore': deleteAllBefore
        }).$promise;
      },
      deleteMetadata: function(){
        return ApiEntitiesService.deleteMetadata().delete({}).$promise;
      },
      getAppInfo: function () {
        return ApiEntitiesService.getAppInfo().get().$promise;
      },
      getCrossDatabases: function() {
        return ApiEntitiesService.getCrossDatabases().get().$promise;
      },
      getCrossTables: function() {
        return ApiEntitiesService.getCrossTables().get().$promise;
      },
      getTableInfo: function(tableName){
        return ApiEntitiesService.getTableInfo().post({
          tableName:tableName
        }).$promise;
      },
      executeQuery: function(query){
        return ApiEntitiesService.executeQuery().post({
          query: query
        }).$promise;
      }
    };
  }
})();
