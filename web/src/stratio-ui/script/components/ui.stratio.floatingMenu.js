'use strict';

(function($){

angular
	.module('StratioUI.components.floatingMenu',[])
	.directive('stFloatingMenu', stFloatingMenu);

stFloatingMenu.$inject = ['TEMPLATE_URL'];
function stFloatingMenu(TEMPLATE_URL){
	var hideFloatingMenu = {};
	var lockToggle = false;

	var directive = {
		restrict: 'AE',
		require: 'ngModel',
		scope: {
			'toggleId': "@",
			'align': "@"
		},
		templateUrl: TEMPLATE_URL('components', 'floatingMenu'),
		transclude: true,
		controller: controller
	};

	return directive;

	controller.$inject = ['$rootScope', '$scope']
	function controller($rootScope, $scope){
		if(!$rootScope.stToggleFloatingMenu)
			$rootScope.stToggleFloatingMenu = {};

		$rootScope.stToggleFloatingMenu[$scope.toggleId] = showFloatingMenu($scope, $rootScope);
		hideFloatingMenu[$scope.toggleId] = initHideFloatingMenu($scope, $rootScope);

		$(window).on('click', hideAllFloatingMenu);
	}

	function showFloatingMenu($scope, $rootScope){
		return function(){
			var isVisible = $scope.visible;

			hideAllFloatingMenu();
			setLockToggle();

			$scope.visible = !isVisible;

			if($rootScope.$$phase == null)
				$scope.$apply();
		}
	}

	function initHideFloatingMenu($scope, $rootScope){
		return function(event){
			$scope.visible = false;

			$(window).off('click', hideFloatingMenu[$scope.toggleId]);

			if($rootScope.$$phase == null)
				$scope.$apply();
		}
	}

	function hideAllFloatingMenu(event){
		if(event)
			event.stopImmediatePropagation();
		if(lockToggle)
			return;
		for(var menu in hideFloatingMenu){
			hideFloatingMenu[menu]();
			$(window).off('click', hideFloatingMenu[menu]);
		}
	}

	function setLockToggle(){
		lockToggle = true;

		setTimeout(function(){
			lockToggle = false;
		}, 20);
	}

}

})(jQuery);