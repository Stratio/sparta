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

angular
	.module('StratioUI.layout.menutab',[])
	.directive('stMenuTab', stMenuTab);

stMenuTab.$inject = ['TEMPLATE_URL'];
function stMenuTab(TEMPLATE_URL){
	var directive = {
		restrict: 'AE',
		require: 'ngModel',
		transclude: true,
		scope: {
			title: '=title',
			visible: '@visible',
			route: '@route',
			sref: '@sref',
			icon: '@icon',
			label: '@label',
			qaref: '@qaref'
		},
		templateUrl: TEMPLATE_URL('layout', 'menutab'),
		controller: controller
	};

	controller.$inject = ["$scope","$state"];

	return directive;

	function controller (  $scope,  $state){
		$scope.$state = $state;
	}
}
