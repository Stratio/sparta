'use strict';

angular
	.module('StratioUI.components.heading', [])
	.directive('stHeading', stHeading);

stHeading.$inject = ['TEMPLATE_URL', 'stPassAllAttributes'];
function stHeading(TEMPLATE_URL, stPassAllAttributes){
	var directive = {
		restrict: 'AE',
		scope: true,
		templateUrl: TEMPLATE_URL('components', 'heading'),
		transclude: true,
		replace: true,
		link: stPassAllAttributes
	};

	return directive;
}
