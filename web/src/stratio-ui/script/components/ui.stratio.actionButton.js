'use strict';

angular
	.module('StratioUI.components.actionButton', [])
	.directive('stActionButton', stActionButton);

stActionButton.$inject = ['TEMPLATE_URL'];
function stActionButton(TEMPLATE_URL){
	var directive = {
		restrict: 'AE',
		scope: true,
		templateUrl: TEMPLATE_URL('components', 'actionButton'),
		transclude: true,
		replace: true
	};

	return directive;
}
