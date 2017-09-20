describe('directive.c-output-field-list-directive', function () {
  beforeEach(module('webApp'));
  var directive, scope, fakeInputList = null;

  beforeEach(inject(function ($httpBackend, $rootScope, $compile) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    $httpBackend.when('GET', 'templates/components/c-output-field-list.tpl.html')
      .respond("<div></div>");

    scope = $rootScope.$new();
    fakeInputList = [];
    scope.inputs = fakeInputList;
    directive = angular.element(' <c-output-field-list data-inputs="inputs"> </c-output-field-list>');

    directive = $compile(directive)(scope);
    scope.$digest();
    $httpBackend.flush();
  }));

  describe("should be able to remove a item from the model by its index", function () {
    var isolatedScope = null;
    var fakeInput1 = "fake input 1";
    var fakeInput2 = "fake input 2";
    var fakeInput3 = "fake input 3";

    describe("if the input to add has been fill out by user", function () {
      beforeEach(function () {
        isolatedScope = directive.isolateScope();
        isolatedScope.inputs.push(fakeInput1);
        isolatedScope.inputs.push(fakeInput2);
        isolatedScope.inputs.push(fakeInput3);
      });

      it("it is removed if index is between 0 and the model length", function () {
        isolatedScope.deleteInput(0);
        expect(isolatedScope.inputs.length).toBe(2);
      });

      it("it is not removed if index is not between 0 and the model length", function () {
        isolatedScope.deleteInput( isolatedScope.inputs.length);
        expect(isolatedScope.inputs.length).toBe(3);
      });

    });
  });

});
