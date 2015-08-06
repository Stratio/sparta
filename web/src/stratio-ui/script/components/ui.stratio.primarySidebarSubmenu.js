'use strict';

angular
	.module('StratioUI.components.primarySidebarSubmenu', [])
	.directive('stPrimarySidebarSubmenu', stPrimarySidebarSubmenu);

stPrimarySidebarSubmenu.$inject = ['TEMPLATE_URL'];
function stPrimarySidebarSubmenu(TEMPLATE_URL){
	var directive = {
		restrict: 'AE',
		scope: true,
		templateUrl: TEMPLATE_URL('components', 'primarySidebarSubmenu'),
		transclude: true,
		link: link
	};
	return directive;

	function link(scope, elm, attrs, ctrl, $transclude){
		scope.title = attrs.title;

		$transclude(function(content){
			scope.hasTransclude = !!content.length;
		})
    }
}
