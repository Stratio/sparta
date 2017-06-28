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
    .controller('CrossdataCtrl', CrossdataCtrl);

  CrossdataCtrl.$inject = ['$scope', 'EntityFactory', '$translate'];

  function CrossdataCtrl($scope, EntityFactory, $translate) {
    /*jshint validthis: true*/
    var vm = this;
    vm.dataBases = [];
    vm.pageSize = 20;
    vm.tables = [];
    vm.filter = {};
    vm.selectedOption = 'CATALOG';
    vm.filterTables = filterTables;
    vm.setPage = setPage;
    vm.sortTableList = sortTableList;
    vm.openTableInfo = openTableInfo;
    vm.getCrossTables = getCrossTables;
    vm.tableReverse = false;
    vm.sortField = 'name';
    vm.executeQuery = executeQuery;
    vm.errorMessage = { type: 'error', text: '', internalTrace: '' };
    vm.successMessage = { type: 'success', text: '', internalTrace: '' };
    vm.menuOptions = [{
      text: '_MENU_TABS_CATALOG_',
      isDisabled: false,
      name: 'CATALOG'
    }, {
      text: '_MENU_TABS_QUERIES_',
      isDisabled: false,
      name: 'QUERIES'
    }];

    init();

    /************************/

    function init(){
        getCrossTables();
    }


    function getDatabases() {
        EntityFactory.getCrossDatabases().then(function(response){
            vm.dataBases = response;
        });
    }

    function getCrossTables(){
        vm.tables = [];
        EntityFactory.getCrossTables().then(function(response){
            vm.tables = response;
        });
    }

    function filterTables(item){
        if(!vm.filter.database || item.database.includes(vm.filter.database)){
            if(!vm.filter.isTemporary && item.isTemporary){
                return false;
            }
            return true;
        }
        return false;
    }

    function openTableInfo(tableRow){
        if(tableRow.open){
            tableRow.open = false;
        } else {
               /*     angular.forEach(vm.tables, function(table){
            table.open = false;
        });*/
            EntityFactory.getTableInfo(tableRow.name).then(function(response){
                tableRow.open = true;
                tableRow.info = response;
            });
        }
    }

    function getQuery(queryResponse){

        getColumns(queryResponse);
        vm.queryResponse = queryResponse;
        generatePagination(queryResponse);
    }

    function getColumns(queryResponse){
        if(queryResponse && queryResponse.length){
            var firstRow = queryResponse[0];
            var columns = [];
            angular.forEach(Object.keys(firstRow), function(column){
                columns.push(column);
            });
            vm.sortField = columns[0];
            vm.queryResponseColumns = columns;
        }
    }

    function sortTableList(fieldName) {
      if (fieldName == vm.sortField) {
        vm.tableReverse = !vm.tableReverse;
      } else {
        vm.tableReverse = false;
        vm.sortField = fieldName;
      }
    }

    function executeQuery(form){
        vm.successMessage.text = '';
        vm.queryResponse = [];
        vm.queryResponseColumns = [];
        vm.paginatedRows = [];
        vm.errorMessage.text = '';
        form.$setSubmitted();
        if(form.$valid){
            EntityFactory.executeQuery(vm.sql).then(function(response){
                getQuery(response);
               $translate('_EXECUTE_QUERY_OK_', { rows:response.length}).then(function(message){
                vm.successMessage.text = message;
               });
            }).catch(function(error){
                if(error.data){
                    var errorMessage = error.data.message ? error.data.message : error.data;

                } else {
                    var errorMessage = error.message;
                }
                vm.errorMessage.text = errorMessage;
            });
        }
    }

    function generatePagination(results){
        vm.pages = Math.ceil(results.length/vm.pageSize);
        vm.currentPage = 0;
        getPaginationLimits();
        vm.paginatedRows = results.slice(0,vm.pageSize);
    }

    function getPaginationLimits(){
        vm.firstElement =  vm.currentPage * vm.pageSize;
        var lastElement = (vm.currentPage+1) *vm.pageSize;
        vm.lastElement  = lastElement > vm.queryResponse.length ? vm.queryResponse.length : lastElement;
    }

    function setPage(pageNumber){
        vm.currentPage = pageNumber;
        vm.paginatedRows = vm.queryResponse.slice(pageNumber*vm.pageSize,(pageNumber+1)*vm.pageSize);
        getPaginationLimits();
    }


    $scope.$on("newTabOptionValue", function (event, value) {
        vm.selectedOption = value;

        if(value === 'CATALOG'){
            getCrossTables();
        } else {

        }
    });
  }
})();
