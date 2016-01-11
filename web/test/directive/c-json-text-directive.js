describe('directive.c-json-text-directive', function () {
  beforeEach(module('webApp'));
  var directive, scope, fakeJson, validJson = null;

  beforeEach(inject(function ($httpBackend, $rootScope, $compile) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    scope = $rootScope.$new();
    fakeJson = {};

    validJson = '{"valid_key": "valid value"}';
    scope.testJson = validJson;

    directive = angular.element('<textarea json-text ng-model="testJson"> </textarea>');
    directive = $compile(directive)(scope);
    scope.$digest();
    $httpBackend.flush();
  }));

  describe("if the input to add has been fill out by user", function () {
    var directiveScope = null;
    beforeEach(function () {
      directiveScope = directive.scope();
    });

    describe("should validate the text container when edit the text from the container element", function () {
      beforeEach(function () {
          scope.testJson = validJson;
          scope.$digest();
        }
      );
      it("if text is a valid json, a valid class is added to element and model is updated with the new json", function () {
        var anotherValidJson = '{"valid_key": "this is is another valid json"}';
        directive.val(anotherValidJson).trigger('input');

        expect(directive.hasClass("ng-valid")).toBeTruthy();
        expect(directive.hasClass("ng-invalid")).toBeFalsy();

        expect(scope.testJson).toEqual(JSON.parse(anotherValidJson));
      });

      it("if text is an invalid json, an error class is added to element and model is set to undefined", function () {
        var invalidJson = '{"invalidJSON": ddd}';

        directive.val(invalidJson).trigger('input');

        expect(directive.hasClass("ng-invalid")).toBeTruthy();
        expect(directive.hasClass("ng-valid")).toBeFalsy();
        expect(scope.testJson).toEqual(undefined);
      });

      it("if text is empty, model is updated to empty json", function () {
        directive.val("").trigger('input');

        expect(directive.hasClass("ng-valid")).toBeTruthy();
        expect(scope.testJson).toEqual({});
      });
    });
  });
});
