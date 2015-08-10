'use strict';

angular
	.module('StratioUI.helper.constant.templateUrl', [])
	.constant('TEMPLATE_URL', function(type, name){
		return 'stratio-ui/template/' + type + '/ui.stratio.' + name + '.html';
	});