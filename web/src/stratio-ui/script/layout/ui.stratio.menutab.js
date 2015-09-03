'use strict';

angular
	.module('StratioUI.layout.menutab',[])
	.directive('stMenuTab', stMenuTab);

stMenuTab.$inject = ['TEMPLATE_URL'];
function stMenuTab(TEMPLATE_URL){
	var directive = {
		restrict: 'AE',
		require: 'ngModel',
		scope: {
			title: '@title',
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
