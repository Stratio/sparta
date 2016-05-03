describe('policies.wizard.factory.policy-factory', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/policyList.json'));

  var factory, ApiPolicyService, scope, q, promiseMock, fakePolicyList, fakePolicy = null;

  beforeEach(module(function ($provide) {
    ApiPolicyService = jasmine.createSpyObj('ApiPolicyService', ['getPolicyByFragmentId', 'getPolicyById',
      'getAllPolicies', 'createPolicy', 'deletePolicy', 'runPolicy', 'stopPolicy', 'savePolicy', 'getPoliciesStatus', 'getFakePolicy', 'downloadPolicy']);

    // inject mocks
    $provide.value('ApiPolicyService', ApiPolicyService);
  }));

  beforeEach(inject(function (PolicyFactory, $httpBackend, $q, _apiPolicyList_, $rootScope) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});
    factory = PolicyFactory;
    q = $q;
    scope = $rootScope.$new();
    fakePolicyList = _apiPolicyList_;
    fakePolicy = fakePolicyList[0].policy;
    promiseMock = jasmine.createSpy('promise').and.callFake(function () {
      return {"$promise": q.defer()};
    });
  }));

  describe("should have a function for each policy api service", function () {
    it("get policy by id", function () {
      var fakePolicyId = "fake policy id";

      ApiPolicyService.getPolicyById.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getPolicyById(fakePolicyId);
      expect(promiseMock).toHaveBeenCalledWith({'id': fakePolicyId});

    });

    it("get policy by fragment id and fragment type", function () {
      var fakeFragmentId = "fake fragment id";
      var fakeFragmentType = "output";
      ApiPolicyService.getPolicyByFragmentId.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getPolicyByFragmentId(fakeFragmentType, fakeFragmentId);

      expect(promiseMock).toHaveBeenCalledWith({'type': fakeFragmentType, 'id': fakeFragmentId});
    });

    it("get sll policies", function () {
      ApiPolicyService.getAllPolicies.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getAllPolicies();

      expect(promiseMock).toHaveBeenCalled();
    });

    it("create a new policy", function () {
      var fakeNewPolicyData = {"fake attribute": "fake value"};
      ApiPolicyService.createPolicy.and.returnValue(
        {
          "create": promiseMock
        });

      factory.createPolicy(fakeNewPolicyData);

      expect(promiseMock).toHaveBeenCalledWith(fakeNewPolicyData);
    });

    it("remove a policy by id", function () {
      var fakePolicyId = "fake policy id";
      ApiPolicyService.deletePolicy.and.returnValue(
        {
          "delete": promiseMock
        });

      factory.deletePolicy(fakePolicyId);

      expect(promiseMock).toHaveBeenCalledWith({"id": fakePolicyId});
    });

    it("run a policy by id", function () {
      var fakePolicyId = "fake policy id";
      ApiPolicyService.runPolicy.and.returnValue(
        {
          "get": promiseMock
        });

      factory.runPolicy(fakePolicyId);

      expect(promiseMock).toHaveBeenCalledWith({"id": fakePolicyId});
    });

    it("stop a policy", function () {
      var fakePolicy = {"fake attribute": "fake value"};
      ApiPolicyService.stopPolicy.and.returnValue(
        {
          "update": promiseMock
        });

      factory.stopPolicy(fakePolicy);

      expect(promiseMock).toHaveBeenCalledWith(fakePolicy);
    });

    it("save a policy", function () {
      var fakePolicy = {"fake attribute": "fake value"};
      ApiPolicyService.savePolicy.and.returnValue(
        {
          "put": promiseMock
        });

      factory.savePolicy(fakePolicy);

      expect(promiseMock).toHaveBeenCalledWith(fakePolicy);
    });

    it("get status of all policies", function () {
      ApiPolicyService.getPoliciesStatus.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getPoliciesStatus();

      expect(promiseMock).toHaveBeenCalledWith();
    });

    it("get a fake policy", function () {
      ApiPolicyService.getFakePolicy.and.returnValue(
        {
          "get": promiseMock
        });

      factory.getFakePolicy();

      expect(promiseMock).toHaveBeenCalledWith();
    });

    it("download a policy by its id", function () {
      var fakePolicyId = "fake policy id";
      ApiPolicyService.downloadPolicy.and.returnValue(
        {
          "get": promiseMock
        });

      factory.downloadPolicy(fakePolicyId);

      expect(promiseMock).toHaveBeenCalledWith({"id": fakePolicyId});
    });
  });


  describe("should be able to find if there is a policy with the introduced name", function () {

    beforeEach(function () {

      ApiPolicyService.getAllPolicies.and.callFake(
        function () {
          return {
            "get": function () {
              var defer = q.defer();
              defer.resolve(fakePolicyList);

              return {"$promise": defer.promise};
            }
          }
        });
    });

    afterEach(function () {
      scope.$apply();
    });

    it("It returns true if there is another policy with the same name and different id", function () {
      factory.existsPolicy(fakePolicy.name, "fake another id").then(function (result) {
        expect(result).toBe(true);
      });
    });

    it("It returns false if there is another policy with the same name and the same id", function () {
      factory.existsPolicy(fakePolicy.name, fakePolicy.id).then(function (result) {
        expect(result).toBe(false);
      });
    });

    it("It returns false if there is not any policy with the same name", function () {
      factory.existsPolicy("fake new name").then(function (result) {
        expect(result).toBe(false);
      });
    });
  })
});
