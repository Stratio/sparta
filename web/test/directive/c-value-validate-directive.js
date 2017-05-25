describe('directive.c-json-text-directive', function () {
  beforeEach(module('webApp', function ($provide) {
    $provide.constant('apiConfigSettings', {
      timeout: 5000
    });
  }));
  var element, scope;

  beforeEach(inject(function ($httpBackend, $rootScope, $compile) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    scope = $rootScope.$new();

    element = angular.element('<form name="form"><input ng-model="modelValue" name="fieldName" c-value-validate="validate" invalid-values="invalidValues"></form>');
    scope.model = null;
    scope.validate = true;
    scope.invalidValues = ["select", "by"];
    $compile(element)(scope);
    form = scope.form;
  }));

  describe("if the input validation is enabled", function () {

    it("if text is a valid string, the form field should be valid", function () {
      form.fieldName.$setViewValue('name');
      scope.$digest();
      expect(scope.modelValue).toEqual('name');
      expect(form.fieldName.$valid).toBe(true);
    });
    it("if text is an invalid string, the form field should be invalid", function () {
      form.fieldName.$setViewValue('by');
      scope.$digest();
      expect(scope.modelValue).toEqual('by');
      expect(form.fieldName.$valid).toBe(false);
    });
  });
});
