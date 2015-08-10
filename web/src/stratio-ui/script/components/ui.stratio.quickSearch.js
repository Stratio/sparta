'use strict';

angular
	.module('StratioUI.components.quickSearch',[])
	.directive('stQuickSearch', stQuickSearch);

stQuickSearch.$inject = ['TEMPLATE_URL'];
function stQuickSearch(TEMPLATE_URL){
	var directive = {
		restrict: 'AE',
		require: 'ngModel',
		scope: {
			'ngModel': '=',
			'ngKeyup': '&',
			'placeholder': '@',
			'value': '@'
		},
		templateUrl: TEMPLATE_URL('components', 'quickSearch'),
		link: link,
		replace: true
	};

	return directive;

	function link(scope, element, attributes, ctrl){
		scope.ngModel = attributes.value;
	}
}
