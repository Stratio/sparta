describe('directive.c-accordion-directive', function () {
  beforeEach(module('webApp'));
  var directive, scope, fakeModel, accordionStatusServiceMock, isolatedScope = null;

  beforeEach(module(function ($provide) {
    accordionStatusServiceMock = jasmine.createSpyObj('AccordionStatusService', ['resetAccordionStatus']);

    // inject mocks
    $provide.value('AccordionStatusService', accordionStatusServiceMock);
  }));

  beforeEach(inject(function ($httpBackend, $rootScope, $compile) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});
    fakeModel = [];
    $httpBackend.when('GET', 'templates/components/c-accordion.tpl.html')
      .respond("<div></div>");

    scope = $rootScope.$new();

    scope.items = [fakeModel];
    scope.accordionStatus = [true];
    directive = angular.element('<c-accordion items="items" accordion-status = "accordionStatus"> </c-accordion>');
    directive = $compile(directive)(scope);
    scope.$digest();
    $httpBackend.flush();

   isolatedScope = directive.isolateScope();
  }));

  it("Should be able to generate an incremental index to each item", function(){
    expect(isolatedScope.generateIndex()).toBe(0);
    expect(isolatedScope.generateIndex()).toBe(1);
    expect(isolatedScope.generateIndex()).toBe(2);
    expect(isolatedScope.generateIndex()).toBe(3);
  });

  it("Should reset the accordion status when it is initialized", function () {
    expect(accordionStatusServiceMock.resetAccordionStatus).toHaveBeenCalledWith(scope.accordionStatus, scope.items.length, scope.items.length);
  });

  it("Should reset the accordion status when an item is added or removed from item array", function(){
    accordionStatusServiceMock.resetAccordionStatus.calls.reset();

    scope.items.push(angular.copy(fakeModel));
    scope.$digest();

    expect(accordionStatusServiceMock.resetAccordionStatus).toHaveBeenCalledWith(scope.accordionStatus, scope.items.length);
  })
});
