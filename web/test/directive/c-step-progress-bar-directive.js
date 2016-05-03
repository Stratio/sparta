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

  describe ("should be able to know when current step message has to be shown", function(){
    var isolatedScope = null;
    var currentStep = 4;
    var steps = [];
    beforeEach( inject(function ($compile, $httpBackend){
      steps =[{name: "step 1"},{name: "step 2"},{name: "step 3"},{name: "step 4"},{name: "step 5"}];

      directive = angular.element('<c-step-progress-bar> </c-step-progress-bar>');
      directive = $compile(directive)(scope);

      scope.$digest();
      $httpBackend.flush();

      isolatedScope = directive.isolateScope();
      isolatedScope.steps = steps;
    }));

    it("if current step is the last step, current step message is shown", function(){
      isolatedScope.current = steps.length-1;
      isolatedScope.nextStepAvailable = true;

      expect(isolatedScope.showCurrentStepMessage()).toBeTruthy();
    });

    it("if next step si not available and next step is not visited, current step message is shown", function(){
      isolatedScope.current = 0;
      isolatedScope.visited[isolatedScope.current+1] = false;
      isolatedScope.nextStepAvailable = false;

      expect(isolatedScope.showCurrentStepMessage()).toBeTruthy();
    });

  })

});
