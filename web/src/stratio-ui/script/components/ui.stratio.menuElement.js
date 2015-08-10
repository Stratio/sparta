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
		link: link,
		link: stPassAllAttributes
	};

	return directive;

	function link(scope, element, attributes){
		if(!scope.forceBind)
			return;

		var bindable = scope.forceBind.split(' ');

		for(var attr in bindable){
			var attribute = bindable[attr];
			scope[attribute] = scope.$parent[attribute];
		}
	}
}
