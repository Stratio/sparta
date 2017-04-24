/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function (angular) {
    'use strict';

    /*LINE WITH LABEL AND ICON*/

    angular
        .module('webApp')
        .directive('cInputFileReader', cInputFileReader);


    function cInputFileReader() {
        return {
            restrict: 'E',
            scope: {
                fileData: '=',
                buttonText: '='
            },
            replace: true,
            templateUrl: 'templates/components/c-input-file-reader.tpl.html',
            link: cInputFileReaderLink
        }
    };

    cInputFileReaderLink.$inject = ['scope', 'element', 'attributes'];

    function cInputFileReaderLink(scope, element, attributes) {

        scope.click = function () {
            angular.element('#uploadFileRead').trigger('click');
        };

        element.bind("change", function (event) {
            var reader = new FileReader();
            reader.readAsText(event.target.files[0]);
            reader.onload = function (loadEvent) {
                scope.$apply(function () {
                    scope.fileData = JSON.stringify(JSON.parse(loadEvent.target.result), null, 3);
                });
            }
        });
    }
})(window.angular);

