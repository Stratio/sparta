(function () {
  'use strict';

  /*LINE WITH A FORM CONTROL AND A LIST OF INPUT FIELDS*/

  angular
    .module('webApp')
    .directive('cTagList', cTagList);


  function cTagList() {
    var directive = {
      restrict: 'E',
      scope: {
        labelControlText: "=",
        labelControlClass: '=',
        tagControlClass: '=',
        formControlClass: "=",
        placeholder: '=',
        tagText: "=",
        model: "=",
        tagType: "=",
        pattern: "=",
        tags: "=",
        readonly: "=",
        enableDelete: "=",
        required: "=",
        showLabel: "=",
        qa: "@",
        help: '@',
        helpQa: '@'
      },
      replace: "true",
      templateUrl: 'templates/components/c-tag-list.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      scope.deleteTag = deleteTag;

      function deleteTag(index) {
        scope.tags.splice(index,1);
      }
    }
  }
})();
