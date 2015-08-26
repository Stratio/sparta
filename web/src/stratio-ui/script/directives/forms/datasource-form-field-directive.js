(function () {
	 'use strict';

	 angular
		  .module('webApp')
		  .directive('datasourceFormField', datasourceFormField);

	 function datasourceFormField() {
		  var directive = {
				link: link,
				templateUrl: 'stratio-ui/template/form/datasource_form_field.html',
				restrict: 'AE',
				replace: true,
				scope: {
					 ngFormId: '@',
					 name: '@stName',
					 field: '=',
					 form: '=',
					 model: '='
				}
		  }
		  return directive;

		  function link(scope, element, attrs) {
				scope.mimeType = getMimeType();
				scope.uploadFrom = '';
				scope.isVisible = function() {
					scope.modify = {};
					 if (scope.field && scope.field.hasOwnProperty('hidden') && scope.field.hidden){
						  return false
					 }
					 if (scope.field && scope.field.hasOwnProperty('visible')) {
						  for (var i=0; i<scope.field.visible.length; i++) {
								var actual = scope.field.visible[i];
								if (actual.value === scope.model[actual[0].propertyId].value) {
									if(scope.field.visible[i].hasOwnProperty('overrideProps')){
										for (var f = 0; f < scope.field.visible[i].overrideProps.length ; f++ ){
											var overrideProps = scope.field.visible[i].overrideProps[f];
											scope.modify[overrideProps.label] = overrideProps.value;
										}
									 }
									 return true;
								}
						  }
						  return false;
					 }
					 return true;
				};

            function getMimeType (){
                var splited = scope.field.propertyType == 'file'? scope.field.propertyName.split(' ') : null;
                var typeFile = splited ? splited[0].toLowerCase() : null;
                var accept;
                switch (typeFile){
                    case 'csv':
                        accept = '.csv';
                        break;
                    case 'json':
                        accept = '.json';
                        break;
                        case 'excel':
                        accept = '.xls, .xlsx';
                        break;
                    case 'xml':
                        accept = '.xml';
                        break;
                    default:
                        accept = '*';
                        break;
                }
                return accept;
            }
        }

    }
})();
