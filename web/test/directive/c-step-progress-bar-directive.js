describe('directive.c-step-progress-bar-directive', function () {
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

  describe("should be able to set the current step only if it is available", function () {
    var newCurrentStep = 5;
    var isolatedScope = null;
    var currentStep = 4;
    beforeEach( inject(function ($compile, $httpBackend){
      scope.currentStep = currentStep;
      directive = angular.element('<c-step-progress-bar current-step = "currentStep"> </c-step-progress-bar>');
      directive = $compile(directive)(scope);

      scope.$digest();
      $httpBackend.flush();

      isolatedScope = directive.isolateScope();
    }));

    it("if selected step is not the next to the current, current step is not changed", function () {
      newCurrentStep= 7;
      isolatedScope.chooseStep(newCurrentStep);
      scope.$digest();
      expect(isolatedScope.current).toBe(currentStep);
    });

    it("if selected step is minor to the current, current step is changed", function () {
      newCurrentStep= 2;
      isolatedScope.chooseStep(newCurrentStep);
      scope.$digest();
      expect(isolatedScope.current).toBe(newCurrentStep);
    });

    it("if selected step is just the follow to the current, only if it is available, current step is changed", function () {
      newCurrentStep= 5;
      isolatedScope.nextStepAvailable= false;

      isolatedScope.chooseStep(newCurrentStep);
      scope.$digest();
      expect(isolatedScope.current).toBe(currentStep);

      isolatedScope.nextStepAvailable= true;
      isolatedScope.chooseStep(newCurrentStep);
      expect(isolatedScope.current).toBe(newCurrentStep);
    });

    it ("should be able to hide the floating message", function(){
      isolatedScope.showHelp = true;

      isolatedScope.hideHelp();

      expect(isolatedScope.showHelp).toBeFalsy();
    });

    it ("should see if next step is available and if it is available, show its message", function(){
      isolatedScope.showHelp = false;
      isolatedScope.nextStepAvailable = true;
      scope.$digest();

      expect(isolatedScope.showHelp).toBeTruthy();
    });
  });

});
