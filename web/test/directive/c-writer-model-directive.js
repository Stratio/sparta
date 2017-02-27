beforeEach(module('template/trigger/transformation.json'));

describe('directive.c-writer-model-directive', function() {
  beforeEach(module('webApp'));
  var directive, scope,  fakeOutputs, outputServiceMock, isolatedScope, fakeTransformationTriggerTemplate = null;
  beforeEach(module(function ($provide) {
    fakeOutputs = [{label: "output1", value: "output1"}, {label: "output2", value: "output2"}, {
      label: "output3",
      value: "output3"
    }, {label: "output4", value: "output4"}, {label: "output5", value: "output5"}];

    outputServiceMock = jasmine.createSpyObj('OutputService', ['generateOutputNameList']);
    $provide.value('OutputService', outputServiceMock);
  }));

  beforeEach(inject(function($httpBackend, $rootScope, $compile, _templateTriggerTransformation_, $q) {

    outputServiceMock.generateOutputNameList.and.callFake(function() {
      var defer = $q.defer();
      defer.resolve(fakeOutputs);

      return defer.promise;
    });
    $httpBackend.when('GET', 'languages/en-US.json')
        .respond({});

    fakeTransformationTriggerTemplate = _templateTriggerTransformation_;
    scope = $rootScope.$new();
    scope.model = {};
    scope.properties = fakeTransformationTriggerTemplate.writer;
    scope.form = {};
    
    $httpBackend.when('GET', 'templates/components/c-writer-model.tpl.html')
        .respond("<div></div>");

    directive = angular.element('<c-writer-model model="model" properties = "properties" form = form> </c-writer-model>');
    directive = $compile(directive)(scope);

    scope.$digest();
    $httpBackend.flush();
    isolatedScope = directive.isolateScope();
  }));

  describe("When it is initialized", function() {

    it('should init property "writer"', function() {
      expect(isolatedScope.model.writer).toBeDefined();
    });
    it('should load the available output list', function() {
      expect(isolatedScope.policyOutputList).toBe(fakeOutputs);
    });
  });
  
  describe("should be able to add outputs to writer", function() {
    it("if selected output is not null and it hasn't been added to trigger yet, selected output is added", function() {
      isolatedScope.selectedPolicyOutput = "";

      isolatedScope.model.writer.outputs = [];

      isolatedScope.addOutput(); // I try to add an empty output

      expect(isolatedScope.model.writer.outputs.length).toBe(0);

      isolatedScope.selectedPolicyOutput = "fake output";

      isolatedScope.addOutput();  // I try to add a new output

      expect(isolatedScope.model.writer.outputs.length).toBe(1);

      isolatedScope.addOutput(); // I try to add the same output

      expect(isolatedScope.model.writer.outputs.length).toBe(1); // output is not added again
    });
  })

});
