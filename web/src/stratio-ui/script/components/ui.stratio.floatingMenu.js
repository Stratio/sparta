(function($){
'use strict';

angular
	.module('StratioUI.components.floatingMenu',[])
	.directive('stFloatingMenu', stFloatingMenu);

stFloatingMenu.$inject = ['TEMPLATE_URL'];
function stFloatingMenu(TEMPLATE_URL){
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

	var hideFloatingMenu = null;

	controller.$inject = ['$rootScope', '$scope']

	function controller($rootScope, $scope){
		if(!$rootScope.stToggleFloatingMenu) {
			$rootScope.stToggleFloatingMenu = [];
		}

		$rootScope.stToggleFloatingMenu[$scope.toggleId] = showFloatingMenu($scope);
		hideFloatingMenu = initHideFloatingMenu($scope, $rootScope);
	}

	function showFloatingMenu($scope){
		return function() {
			$scope.visible = !$scope.visible;
			$(window).on('click', hideFloatingMenu);
		}
	}

	function initHideFloatingMenu($scope, $rootScope){
		return function(event){
			event.stopPropagation();

			$scope.visible = false;

			$(window).off('click', hideFloatingMenu);

			if($rootScope.$$phase == null){
				$scope.$apply();
			}
		}
	}
}

})(jQuery);