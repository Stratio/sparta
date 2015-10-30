describe('directive.c-step-directive', function () {
  beforeEach(module('webApp'));
  var directive, scope = null;

  beforeEach(inject(function ($httpBackend, $rootScope) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    $httpBackend.when('GET', 'templates/components/c-step-progress-bar.tpl.html')
      .respond("<div></div>");

    scope = $rootScope.$new();

    $httpBackend.flush();
  }));

  it("should be able to set the current step", inject(function ($compile, $httpBackend) {
    var newCurrentStep = 5;
    directive = angular.element(' <c-step-progress-bar current-step = "currentStep"> </c-step-progress-bar>');

    directive = $compile(directive)(scope);
    scope.$digest();
    $httpBackend.flush();

    var isolatedScope = directive.isolateScope();
    isolatedScope.chooseStep(newCurrentStep);
    scope.$digest();
    expect(isolatedScope.current).toBe(newCurrentStep);

    newCurrentStep = 4;
    isolatedScope.chooseStep(newCurrentStep);

    expect(isolatedScope.current).toBe(newCurrentStep);

  }));

});
