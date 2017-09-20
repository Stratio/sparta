describe('policies.wizard.service.output-service', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/outputList.json'));

  var service, $q, rootScope, fragmentFactoryMock, fakeOutputList = null;

  beforeEach(module(function ($provide) {
    fragmentFactoryMock = jasmine.createSpyObj('FragmentFactory', ['getFragments']);
    $provide.value('FragmentFactory', fragmentFactoryMock);
  }));

  beforeEach(inject(function ($httpBackend, OutputService, _apiOutputList_, _$q_, $rootScope) {
    rootScope = $rootScope;
    fakeOutputList = _apiOutputList_;
    service = OutputService;
    $q = _$q_;

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    fragmentFactoryMock.getFragments.and.callFake(function () {
      var defer = $q.defer();

      defer.resolve(fakeOutputList);

      return defer.promise;
    });
  }));


  it("should be able to generate a name and value list of the all available outputs", function () {


    service.generateOutputNameList().then(function (outputNameList) {
      expect(outputNameList.length).toBe(fakeOutputList.length);
      expect(outputNameList[0].label).toEqual(fakeOutputList[0].name);
      expect(outputNameList[0].value).toEqual(fakeOutputList[0].name);
      expect(outputNameList[outputNameList.length - 1].label).toEqual(fakeOutputList[fakeOutputList.length - 1].name);
      expect(outputNameList[outputNameList.length - 1].value).toEqual(fakeOutputList[fakeOutputList.length - 1].name);
    });

    rootScope.$apply();

  });

  it("should be able to ask for available output list to Fragment factory", function () {
    service.getOutputList().then(function (outputList) {
      expect(outputList).toBe(fakeOutputList);
    });

    rootScope.$apply();
  });
});
