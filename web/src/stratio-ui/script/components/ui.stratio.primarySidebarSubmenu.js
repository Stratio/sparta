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
	.module('StratioUI.components.primarySidebarSubmenu', [])
	.directive('stPrimarySidebarSubmenu', stPrimarySidebarSubmenu);

stPrimarySidebarSubmenu.$inject = ['TEMPLATE_URL'];
function stPrimarySidebarSubmenu(TEMPLATE_URL){
	var directive = {
		restrict: 'AE',
		scope: true,
		templateUrl: TEMPLATE_URL('components', 'primarySidebarSubmenu'),
		transclude: true,
		link: link
	};
	return directive;

	function link(scope, elm, attrs, ctrl, $transclude){
		scope.title = attrs.title;
		scope.submenuClass = attrs.submenuClass;

		$transclude(function(content){
			scope.hasTransclude = !!content.length;
		})
    }
}
