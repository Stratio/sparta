'use strict';

angular
	.module('StratioUI.components.primarySidebarTitle', [])
	.directive('stPrimarySidebarTitle', stPrimarySidebarTitle);

stPrimarySidebarTitle.$inject = ['TEMPLATE_URL'];
function stPrimarySidebarTitle(TEMPLATE_URL){
	var directive = {
		restrict: 'AE',
		scope: {
			'classIcon': "@"
		},
		templateUrl: TEMPLATE_URL('components', 'primarySidebarTitle'),
		transclude: true
	};

	return directive;
}
