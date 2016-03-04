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
/**
 * Breaking Bad grid layout.
 * Converts a list in a grid of elements with absolute position to apply CSS3 move animations.
 * @namespace BrBa
 */
(function(angular) {
    'use strict';

    angular
        .module('ui.stratio.grid', [])
        .controller('BrBaController', BrBaController)
        .directive('brBaContainer', brBaContainer)
        .directive('brBaItem', brBaItem)
        .constant('BrBaConfig', {
            columns: 4,         // columns: number of columns. When 0 is based on itemWidth.
            itemHeight: 210,    // itemHeight: item height in pixels. If columns > 0 used to get aspect/ratio.
            itemWidth: 300,     // itemWidth: item width in pixels. If columns > 0 takes priority over 'columns' as minimum width.
            horizontalGap: 30,  // horizontalGap: gap between columns.
            verticalGap: 30,     // verticalGap: gap between rows.
            keepAspectRatio: false
        });


    /**
     * @namespace BrBaController
     * @desc Main controller
     * @memberOf BrBa
     */
    BrBaController.$inject = ['$scope', '$element', '$window', 'BrBaConfig'];
    function BrBaController($scope, $element, $window, BrBaConfig) {
        var vm = this;

        // Initializations
        for (var key in BrBaConfig) {
            vm[key] = BrBaConfig[key];
        }
        for (var key in $scope.props) {
            vm[key] = $scope.props[key];
        }

        vm.isLiquid = vm.columns > 0;
        vm.actual = setGeneralSizes();


        // Listen to resize event
        angular.element($window).on('resize', function(e) {
            if (vm.totalWidth != $element[0].clientWidth) {
                vm.actual = setGeneralSizes();
                $scope.$broadcast('refresh::refresh');
                if(!$scope.$$phase) {
                    $scope.$apply();
                }
            }
        });

        vm.getStyle = function(index, total) {
            var item = {};
            if (vm.totalWidth == $element[0].clientWidth) {
                item.top =  Math.floor(index/vm.actual.numColumns)*(vm.actual.rowHeight+vm.verticalGap)+'px';
                item.left = index%vm.actual.numColumns*(vm.actual.columnWidth+vm.horizontalGap)+'px';
                item.height = vm.actual.rowHeight+'px';
                item.width = vm.actual.columnWidth+'px';
                if (total != vm.totalItems) {
                    vm.totalItems = total;
                    setTotalHeight(total);
                }
            } else {
                vm.actual = setGeneralSizes();
            }
            return item;
        };

        /**
         * @desc Set sizes
         * @memberOf BrBaController
         * @return {object} sizes
         */
        function setGeneralSizes() {
            var actual = {};
            vm.totalWidth = $element[0].clientWidth;
            vm.totalItems = 0;
            actual.columnWidth = getColumnWidth();
            actual.rowHeight = getRowHeight(actual.columnWidth);
            actual.numColumns = getNumColumns(actual.columnWidth);
            return actual;
        }

        /**
         * @desc Get the column width in pixels
         * @memberOf BrBaController
         * @return {int} The column width
         */
          function getColumnWidth() {
            var columnWidth = vm.itemWidth;
            if (vm.isLiquid) {
              var totalMargin = vm.horizontalGap * (vm.columns-1);
              var liquidColumnWidth = Math.floor((vm.totalWidth - totalMargin)/vm.columns);
              var numColumns = vm.columns-1;
              while (liquidColumnWidth<columnWidth && numColumns>0) {
                totalMargin = vm.horizontalGap * (numColumns-1);
                liquidColumnWidth = Math.floor((vm.totalWidth - totalMargin)/numColumns);
                numColumns--;
              }
              columnWidth = liquidColumnWidth>columnWidth ? liquidColumnWidth : columnWidth;
            }
            return columnWidth;
          }

        /**
         * @desc Get the row height in pixels
         * @memberOf BrBaController
         * @param {int} columnWidth The actual column width
         * @return {int} The row height
         */
        function getRowHeight(columnWidth) {
            var rowHeight = vm.itemHeight;
            if (vm.isLiquid && vm.keepAspectRatio) {
                var aspectRatio = vm.itemWidth / vm.itemHeight;
                rowHeight = Math.round(columnWidth / aspectRatio);
            }
            return rowHeight;
        }

        /**
         * @desc Get the number of columns
         * @memberOf BrBaController
         * @param {int} columnWidth The actual column width
         * @return {int} The number of columns
         */
        function getNumColumns(columnWidth) {
            var numColumns = Math.floor((vm.totalWidth+vm.horizontalGap)/(columnWidth+vm.horizontalGap));
            return numColumns>0 ? numColumns : 1;
        }

        /**
         * @desc Get the number of columns
         * @memberOf BrBaController
         */
        function setTotalHeight() {
            var totalRows = Math.ceil(vm.totalItems/vm.actual.numColumns);
            var totalHeight = ((totalRows*vm.actual.rowHeight + (totalRows-1)*vm.verticalGap)+45)+'px';
            $element.css({height: totalHeight});
        }
    }


    /**
     * @namespace brBaContainer
     * @desc br-ba-container directive
     * @memberOf BrBa
     */
    function brBaContainer() {
        var directive = {
            controller: BrBaController,
            controllerAs: 'vm',
            replace: true,
            restrict: 'A',
            scope: {
                total: '=',
                props: '=?brBaContainer'
            },
            template: '<ul ng-transclude class="br-ba-container"></ul>',
            transclude: true
        };
        return directive;
    }


    /**
     * @namespace brBaItem
     * @desc br-ba-item directive
     * @memberOf BrBa
     */
    function brBaItem() {
        var directive = {
            link: link,
            replace: true,
            require: '^brBaContainer',
            restrict: 'A',
            scope: {
                props: '=?brBaItem'
            },
            template: '<li ng-transclude class="br-ba-item" ng-style="getStyle()"></li>',
            transclude: true
        };
        return directive;

        // link
        function link(scope, element, attrs, ctrl) {

            /**
             * @name getStyle
             * @desc get styles from parent
             * @return {object} In valid CSS format
             * @memberOf brBaItem
             */
            scope.getStyle = function() {
                if (scope.props.hasOwnProperty('index') && scope.props.hasOwnProperty('total')) {
                    return ctrl.getStyle(scope.props.index, scope.props.total);
                }
            };

            // Listen to parent event
            scope.$on('refresh::refresh', function() {
                scope.getStyle();
            });
        }
    }

})(angular);
