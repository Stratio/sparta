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

/**
* @desc Base dashboard for Stratio
* @example <st-dashboard></st-dashboard>
*/
(function($){

angular
	.module('StratioUI.layout.dashboard',[])
	.directive('stDashboard', stDashboard);

stDashboard.$inject = ['TEMPLATE_URL'];
function stDashboard(TEMPLATE_URL){
	var compactMenuScroll = 70;

	var directive = {
		restrict: 'AE',
		scope: true,
		transclude: true,
		templateUrl: TEMPLATE_URL('layout', 'dashboard'),
		controller: controller,
		link: link,
		replace: true
	};

	controller.$inject = ['$scope','$rootScope','$timeout'];

	return directive;

	function controller (  $scope,  $rootScope,  $timeout){
		var compactMenuWindowWidth = 0;

		$scope.menuCompact =  false;
		$scope.showAuxiliarSidebar = true;
		$scope.showAuxiliarSidebarSticky = false;
		$scope.showPrimarySidebar = false;
		$scope.toggleAuxiliarSidebar = toggleAuxiliarSidebar;
		$scope.togglePrimarySidebar = togglePrimarySidebar;

		init();

		function init(){
			initOnScroll();
			initOnResize();
			initOnHeaderResize();

			$timeout(checkMenuMode, 200);
		}

		function togglePrimarySidebar(force){
			$scope.showPrimarySidebar = typeof force == 'boolean' ? force : !$scope.showPrimarySidebar;
			updateCharts();
		}

		function toggleAuxiliarSidebar(){
			$scope.showAuxiliarSidebar = !$scope.showAuxiliarSidebar;
			updateCharts();
		}

		function initOnScroll(){
			$(window).scroll(onScroll);
			$(window).scroll(function(){setTimeout(onScroll, 10);});

			function onScroll(){
				$scope.fixedDashboard = ($(document).scrollTop() >= compactMenuScroll);
				if($rootScope.$$phase == null)
					$scope.$apply();

				checkAuxiliarSticky();
				updateStickyPosition();
				updateHeaderSize();
			}

			$scope.$on('force-on-header-resize', onScroll);
		}
		function initOnResize(){
			$(window).resize(function(){
				checkAuxiliarSticky();
				updateStickyPosition();
				checkMenuMode();
			});
		}
		function initOnHeaderResize(){
			$scope.$on('on-header-resize', function(event, size){
				$('#base-primary-sidebar').css('top', size + 'px');
			});
		}

		function updateCharts(){
			$timeout(function(){
				$scope.$broadcast('NV_UPDATE_CHARTS');
			}, 320);
		}

		function checkMenuMode(){
			var menu = $('#stratio-base .menu-cont');
			var menuWidth = 0;

			menu.children().each(function(i, e){
				menuWidth += $(e).outerWidth();
			});

			if(!$scope.menuCompact || compactMenuWindowWidth < menu.width()){
				$scope.menuCompact = menu.width() < menuWidth;
				if($scope.menuCompact)
					compactMenuWindowWidth = menuWidth;
			}

			$scope.$apply();
		}

		function checkAuxiliarSticky(){
			$scope.showAuxiliarSidebarSticky = $(window).height() <= ($('#base-auxiliar-sidebar').height() + 40 + 30);
			if($rootScope.$$phase == null)
				$scope.$apply();
		}

		function updateStickyPosition(){
			var sidebar = $('#base-auxiliar-sidebar');

			if(!$scope.showAuxiliarSidebarSticky){
				sidebar.css('margin-top', '');
				return;
			}
			var scroll = $(window).scrollTop();
			var wHeight = $(window).height();
			var elementBottom = sidebar.offset().top + sidebar.height() + 30;

			if((scroll + wHeight) > elementBottom)
				sidebar.css('margin-top', (scroll + wHeight - sidebar.height() - 200) + "px");

			if((scroll + 100) < sidebar.offset().top)
				sidebar.css('margin-top', (scroll - 70) + "px");
		}

		function updateHeaderSize(){
			var visibleHeaderHeight = $('.stratio-base-header').height() - $(document).scrollTop();
			var minVisibleHeaderHeight = compactMenuScroll;

			$rootScope.$broadcast('on-header-resize', Math.max(visibleHeaderHeight, minVisibleHeaderHeight));
		}
	}

	function link(scope, element, attributes){
		for(var attr in attributes){
			if(typeof attributes[attr] == 'string'){
				scope[attr] = attributes[attr];
			}
		}
	}
}

})(jQuery);
