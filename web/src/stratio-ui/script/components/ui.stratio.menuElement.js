'use strict';

angular
	.module('StratioUI.components.menuElement',[])
	.directive('stMenuElement', stMenuElement);

stMenuElement.$inject = ['TEMPLATE_URL', 'stPassAllAttributes'];
function stMenuElement(TEMPLATE_URL, stPassAllAttributes){
	var directive = {
		restrict: 'AE',
		scope: true,
		templateUrl: TEMPLATE_URL('components', 'menuElement'),
		transclude: true,
		link: stPassAllAttributes
	};

	return directive;
}
