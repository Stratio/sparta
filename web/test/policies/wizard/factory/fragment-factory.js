describe('policies.wizard.factory.fragment-factory', function () {
  beforeEach(module('webApp'));

  var factory, ApiFragmentService, q, promiseMock = null;

  beforeEach(module(function ($provide) {
    ApiFragmentService = jasmine.createSpyObj(['deleteFragment', 'createFragment', 'updateFragment', 'getFragmentById',
      'getFragments', 'getFakeFragments']);

    // inject mocks
    $provide.value('ApiFragmentService', ApiFragmentService);
  }));

  beforeEach(inject(function (FragmentFactory, $q) {
    factory = FragmentFactory;
    q = $q;


    promiseMock = jasmine.createSpy('promise').and.callFake(function () {
      return {"$promise": q.defer()};
    });

  }));

  describe("should have a function for each fragment api service", function () {
    it("delete fragment by fragment id and fragment type", function () {
      var fakeFragmentId = "fake fragment id";
      var fakeFragmentType = "output";

      ApiFragmentService.deleteFragment.and.returnValue(
        {
          "delete": promiseMock
        });

      factory.deleteFragment(fakeFragmentType, fakeFragmentId);
      expect(promiseMock).toHaveBeenCalledWith({'type': fakeFragmentType ,'fragmentId': fakeFragmentId});
    });

    it("create fragment", function () {
      var fakeFragmentData = {"any": "fake fragment attribute"};
      ApiFragmentService.createFragment.and.returnValue(
        {
          "create": promiseMock
        });

      factory.createFragment(fakeFragmentData);
      expect(promiseMock).toHaveBeenCalledWith(fakeFragmentData);
    });

    it("create fragment", function () {
      var fakeFragmentData = {"any": "fake fragment attribute"};
      ApiFragmentService.updateFragment.and.returnValue(
        {
          "update": promiseMock
        });

      factory.updateFragment(fakeFragmentData);
      expect(promiseMock).toHaveBeenCalledWith(fakeFragmentData);
    });

    it("get fragment by id", function () {
      var fakeFragmentId = "fake fragment id";
      var fakeFragmentType = "output";
      ApiFragmentService.getFragmentById.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getFragmentById(fakeFragmentType, fakeFragmentId);
      expect(promiseMock).toHaveBeenCalledWith({'type': fakeFragmentType ,'fragmentId': fakeFragmentId});
    });

    it("get all fragment by fragment type", function () {
     var fakeFragmentType = "output";
      ApiFragmentService.getFragments.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getFragments(fakeFragmentType);
      expect(promiseMock).toHaveBeenCalledWith({'type': fakeFragmentType});
    });

    it("get a list of fake fragments by fragment type", function () {
      var fakeFragmentType = "output";
      ApiFragmentService.getFakeFragments.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getFakeFragments(fakeFragmentType);
      expect(promiseMock).toHaveBeenCalledWith({'type': fakeFragmentType});
    });
  });


})
;
