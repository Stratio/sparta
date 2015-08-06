'use strict';

angular
	.module('StratioUI.components.menuElement',[])
	.directive('stMenuElement', stMenuElement);

stMenuElement.$inject = ['TEMPLATE_URL'];
function stMenuElement(TEMPLATE_URL){
	var directive = {
		restrict: 'AE',
		scope: {
			'href': '@',
			'sref': '@',
			'classIcon': '@',
			'forceBind': '@'
		},
		templateUrl: TEMPLATE_URL('components', 'menuElement'),
		transclude: true,
		link: link
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
