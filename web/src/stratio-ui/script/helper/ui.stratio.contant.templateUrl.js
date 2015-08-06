'use strict';

angular
	.module('StratioUI.helpers.constants', [])
	.constant('TEMPLATE_URL', function(type, name){
		return 'stratio-ui/template/' + type + '/ui.stratio.' + name + '.html';
	});