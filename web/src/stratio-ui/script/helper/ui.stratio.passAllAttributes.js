'use strict';

angular
	.module('StratioUI.helper.passAllAttributes', [])
	.factory('stPassAllAttributes', function(){return function(scope, element, attributes){
		for(var attr in attributes){
			if(typeof attributes[attr] == 'string'){
				scope[attr] = attributes[attr];
			}
		}
	}});