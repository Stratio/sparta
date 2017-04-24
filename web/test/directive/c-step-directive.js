describe('directive.c-step-directive', function () {
  beforeEach(module('webApp'));
  var directive, scope, isolatedScope = null;

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
      directive = angular.element(' <c-step index = "index" current-step = "currentStep"></c-step>');

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

  describe("should be able to return if an step has been visited", function () {

    beforeEach(inject(function ($compile, $httpBackend) {
      directive = angular.element('<c-step index = "index" current-step = "currentStep"> </c-step>');

      directive = $compile(directive)(scope);
      scope.$digest();
      $httpBackend.flush();
      isolatedScope = directive.isolateScope();
      isolatedScope.hasBeenVisited = false;

    }));

    it("a step is visited if current step is major than it", function () {
      isolatedScope.index = 2;
      isolatedScope.current = 3;
      expect(isolatedScope.isVisited()).toBeTruthy();
    });

    it("a step is visited if hasBeenVisited variable is true and it is not the current step", function () {
      isolatedScope.hasBeenVisited = true;
      isolatedScope.index = 4;
      isolatedScope.current = 3;

      expect(isolatedScope.isVisited()).toBeTruthy();
    });
  });

  describe("should be able to return if an step has been enabled", function () {
    beforeEach(inject(function ($compile, $httpBackend) {
      directive = angular.element('<c-step index = "index" current-step = "currentStep"> </c-step>');

      directive = $compile(directive)(scope);
      scope.$digest();
      $httpBackend.flush();
      isolatedScope = directive.isolateScope();
      isolatedScope.hasBeenVisited = false;

    }));

    it("step is enabled if it is the next to the current step and isAvailable variable is true", function () {
      isolatedScope.index = 2;
      isolatedScope.current = 1;
      isolatedScope.isAvailable = true;

      expect(isolatedScope.isEnabled()).toBeTruthy();


      isolatedScope.isAvailable = false;

     // expect(isolatedScope.isEnabled()).toBeFalsy();

      isolatedScope.index = 2;
      isolatedScope.current = 2;
      isolatedScope.isAvailable = true;

     // expect(isolatedScope.isEnabled()).toBeFalsy();
    });
  });

});
