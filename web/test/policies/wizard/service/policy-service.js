describe('service.policy-service', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/outputList.json'));
  beforeEach(module('model/policy.json'));

  var service, q, UtilsService, rootScope, httpBackend, PolicyModelFactoryMock, OutputServiceMock, fakeOutputList, UtilsServiceMock = null;
  var fakePolicy = {};

  beforeEach(module(function ($provide) {
    PolicyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy']);
    UtilsServiceMock = jasmine.createSpyObj('UtilsService', ['removeDuplicatedJSONs', 'convertDottedPropertiesToJson'
      , 'getFilteredJSONByArray']);
    OutputServiceMock = jasmine.createSpyObj('OutputService', ['getOutputList']);

    // inject mocks
    $provide.value('PolicyModelFactory', PolicyModelFactoryMock);
    //  $provide.value('UtilsService', UtilsServiceMock);
    $provide.value('OutputService', OutputServiceMock);

    PolicyModelFactoryMock.getCurrentPolicy.and.returnValue(fakePolicy);

  }));

  beforeEach(inject(function (PolicyService, $q, $rootScope, $httpBackend, _modelPolicy_, _apiOutputList_, _UtilsService_) {
    q = $q;
    httpBackend = $httpBackend;
    rootScope = $rootScope;
    service = PolicyService;
    fakeOutputList = _apiOutputList_;
    UtilsService = _UtilsService_;
    fakePolicy = angular.merge(fakePolicy, angular.copy(_modelPolicy_));
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    OutputServiceMock.getOutputList.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakeOutputList);

      return defer.promise;
    });

  }));

  describe("when it is initialized", function () {

    it('it should get the policy that is being created or edited from policy factory', function () {
      expect(service.policy).toBe(fakePolicy);
    });
  });

  describe("should be able to generate a policy json with the agreed format expected by API server", function () {
    it("should convert the description attributes", function () {
      fakePolicy.rawDataEnabled = true;

      service.generateFinalJSON().then(function (finalJson) {
        expect(finalJson.rawData.path).toBeTruthy();
        expect(finalJson.sparkStreamingWindow).toEqual(fakePolicy.sparkStreamingWindowNumber + fakePolicy.sparkStreamingWindowTime);
        expect(finalJson.rawDataEnabled).toBeUndefined();
      });

      rootScope.$apply();

      fakePolicy.rawDataEnabled = false;
      service.generateFinalJSON().then(function (finalJson) {
        expect(finalJson.rawData.path).toBeUndefined();
        expect(finalJson.rawDataEnabled).toBeUndefined();
      });
      rootScope.$apply();
    });

    it("should convert the over last attribute of stream triggers", function () {
      service.generateFinalJSON().then(function (finalJson) {
        for (var i = 0; i < finalJson.streamTriggers.length; ++i) {
          expect(finalJson.streamTriggers[i].overLast).toBe(fakePolicy.streamTriggers[i].overLastNumber + fakePolicy.streamTriggers[i].overLastTime);
          expect(finalJson.streamTriggers[i].overLastNumber).toBeUndefined();
          expect(finalJson.streamTriggers[i].overLastTime).toBeUndefined();
        }
      });
      rootScope.$apply();
    });

    it("should convert the compute every attribute of stream triggers", function () {
      service.generateFinalJSON().then(function (finalJson) {
        for (var i = 0; i < finalJson.streamTriggers.length; ++i) {
          expect(finalJson.streamTriggers[i].computeEvery).toBe(fakePolicy.streamTriggers[i].computeEveryNumber + fakePolicy.streamTriggers[i].computeEveryTime);
          expect(finalJson.streamTriggers[i].computeEveryNumber).toBeUndefined();
          expect(finalJson.streamTriggers[i].computeEveryTime).toBeUndefined();
        }
      });
      rootScope.$apply();
    });

    it("should add an output list searching for all used outputs in policy", function () {
      var expectedOutputLength = fakePolicy.streamTriggers[0].writer.outputs.length
        + fakePolicy.cubes[0]['writer.outputs'].length
        + fakePolicy.cubes[0].triggers[0].writer.outputs.length;
      spyOn(UtilsService, 'removeDuplicatedJSONs').and.callThrough();
      service.generateFinalJSON().then(function (finalJson) {
        //fragment length has to be input + outputs list
        expect(finalJson.fragments.length).toBe(expectedOutputLength + 1);

        //should remove duplicated outputs
        expect(UtilsService.removeDuplicatedJSONs).toHaveBeenCalled();
      });
      rootScope.$apply();
    });

    it("should remove unused attributes", function () {
      fakePolicy.rawDataEnabled = false;

      service.generateFinalJSON().then(function (finalJson) {
        expect(finalJson.rawDataPath).toBeUndefined();
        expect(finalJson.rawDataEnabled).toBeUndefined();
        expect(finalJson.checkpointPath).toBeUndefined();
        expect(finalJson.autoDeleteCheckpoint).toBeUndefined();
        expect(finalJson.sparkStreamingWindowNumber).toBeUndefined();
        expect(finalJson.sparkStreamingWindowTime).toBeUndefined();
        expect(finalJson.rawData.path).toBeUndefined();
      });
      rootScope.$apply();

      fakePolicy.rawDataEnabled = true;

      service.generateFinalJSON().then(function (finalJson) {
        expect(finalJson.rawDataPath).toBeUndefined();
        expect(finalJson.rawDataEnabled).toBeUndefined();
        expect(finalJson.sparkStreamingWindowNumber).toBeUndefined();
        expect(finalJson.sparkStreamingWindowTime).toBeUndefined();
        expect(finalJson.rawData.path).not.toBeUndefined();
      });

      rootScope.$apply();
    })
  });
});
