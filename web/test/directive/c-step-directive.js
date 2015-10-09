describe('directive.c-step-directive', function () {
  beforeEach(module('webApp'));
  var directive, scope = null;

  beforeEach(inject(function ($httpBackend, $rootScope) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    $httpBackend.when('GET', 'templates/components/c-step.tpl.html')
      .respond("<div></div>");

    scope = $rootScope.$new();

    $httpBackend.flush();
  }));


  describe("should be able to return if a step is selected or not", function () {
    it("if current and index are the same, returns true", inject(function ($compile, $httpBackend) {
      scope.index = 0;
      scope.currentStep = 0;
      directive = angular.element(' <c-step index = "index" current-step = "currentStep"> </c-step>');

      directive = $compile(directive)(scope);
      scope.$digest();
      $httpBackend.flush();

      var isolatedScope = directive.isolateScope();
      expect(isolatedScope.isSelected()).toBeTruthy();
    }));

    it("if current and index are different, returns false", inject(function ($compile, $httpBackend) {
      scope.index = 1;
      scope.currentStep = 2;
      directive = angular.element('<c-step index = "index" current-step = "currentStep"> </c-step>');

      directive = $compile(directive)(scope);
      scope.$digest();
      $httpBackend.flush();

      var isolatedScope = directive.isolateScope();
      expect(isolatedScope.isSelected()).toBeFalsy();
    }));
  });


});
