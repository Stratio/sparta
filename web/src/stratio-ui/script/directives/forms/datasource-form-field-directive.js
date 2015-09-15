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
					 model: '=',
					 qa: '@'
				}
		  }
		  return directive;

		  function link(scope, element, attrs) {
				scope.mimeType = getMimeType();
				scope.uploadFrom = '';
				scope.isVisible = function () {
		            scope.modify = {};
		            if (scope.field && scope.field.hasOwnProperty('hidden') && scope.field.hidden) {
		               return false;
		            }
		            if (scope.field && scope.field.hasOwnProperty('visible')) {
		               for (var i = 0; i < scope.field.visible.length; i++) {
		                  var actuals = scope.field.visible[i];
		                  var allTrue = true;
		                  for (var j = 0; j < actuals.length; j++) {
		                     var actual = actuals[j];
		                     /*if (actual.value === scope.model[actual.propertyId].value) {*/
		                     if (actual.value === scope.model[actual.propertyId]) {
		                        if (actual.hasOwnProperty('overrideProps')) {
		                           for (var f = 0; f < actual.overrideProps.length; f++) {
		                              var overrideProps = actual.overrideProps[f];
		                              scope.modify[overrideProps.label] = overrideProps.value;
		                              scope.field[overrideProps.label] = overrideProps.value;
		                           }
		                        }
		                     } else {
		                        allTrue = false;
		                        break; //TODO: check this break
		                     }
		                  }
		                  if (allTrue) {
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
