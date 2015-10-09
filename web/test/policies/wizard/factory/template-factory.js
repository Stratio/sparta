describe('policies.wizard.factory.template-factory', function () {
  beforeEach(module('webApp'));

  var factory, ApiTemplateService, q, promiseMock = null;

  beforeEach(module(function ($provide) {
    ApiTemplateService = jasmine.createSpyObj(['getFragmentTemplateByType', 'getPolicyTemplate']);

    // inject mocks
    $provide.value('ApiTemplateService', ApiTemplateService);
  }));

  beforeEach(inject(function (TemplateFactory, $q) {
    factory = TemplateFactory;
    q = $q;


    promiseMock = jasmine.createSpy('promise').and.callFake(function () {
      return {"$promise": q.defer()};
    });

  }));

  describe("should have a function for each template api service", function () {
    it("get new fragment template by fragment type", function () {
      var fakeFragmentType = "output";

      ApiTemplateService.getFragmentTemplateByType.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getNewFragmentTemplate(fakeFragmentType);
      expect(promiseMock).toHaveBeenCalledWith({'type': fakeFragmentType + '.json'});
    });

    it("get policy template", function () {
      ApiTemplateService.getPolicyTemplate.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getPolicyTemplate();
      expect(promiseMock).toHaveBeenCalledWith();

    });
  });


})
;
