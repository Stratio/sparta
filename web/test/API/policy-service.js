describe('API.policy-service', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/policy.json'));
  beforeEach(module('api/policyList.json'));
  beforeEach(module('api/policiesStatusList.json'));

  var srv, rootScope, httpBackend, fakePolicyById, fakePolicyList, fakePoliciesStatusList = null;

  beforeEach(
    inject(function ($httpBackend, $rootScope, _ApiPolicyService_, _apiPolicy_, _apiPolicyList_, _apiPoliciesStatusList_) {
      fakePolicyById 					= _apiPolicy_;
      fakePolicyList 					= _apiPolicyList_;
      fakePoliciesStatusList 	= _apiPoliciesStatusList_;
      srv 										= _ApiPolicyService_;
      rootScope 							= $rootScope;
      httpBackend 						= $httpBackend;

      httpBackend.when('GET', 'languages/en-US.json').respond({});
    })
  );

  it("Should return a policy by Id", function () {
    var policyIdJSON = {"id":"2581f20a-fd83-4315-be45-192bc5sEdFff"};
    httpBackend.when('GET', '/policy/find/2581f20a-fd83-4315-be45-192bc5sEdFff').respond(fakePolicyById);

    srv.getPolicyById().get(policyIdJSON).$promise.then(function(result){
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakePolicyById));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should return a policy by fragment Id", function () {
    var fragmentTypeIdJSON = {"type": "input", "id": "2581f20a-av83-4315-be45-192bc5sEdFff"};
    httpBackend.when('GET', '/policy/fragment/input/2581f20a-av83-4315-be45-192bc5sEdFff').respond(fakePolicyList);

    srv.getPolicyByFragmentId().get(fragmentTypeIdJSON).$promise.then(function(result){
    	expect(JSON.stringify(result)).toEqual(JSON.stringify(fakePolicyList));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should return all the policies", function () {
  	httpBackend.when('GET', '/policy/all').respond(fakePolicyList);

  	srv.getAllPolicies().get().$promise.then(function(result){
  		expect(JSON.stringify(result)).toEqual(JSON.stringify(fakePolicyList));
  	});

  	rootScope.$digest();
    httpBackend.flush();
  });

  it("Should create a policy", function () {
  	httpBackend.when('POST', '/policy').respond(fakePolicyById);

  	srv.createPolicy().create().$promise.then(function(result){
  		expect(JSON.stringify(result)).toEqual(JSON.stringify(fakePolicyById));
  	});

  	rootScope.$digest();
    httpBackend.flush();
  });

	it("Should delete a policy by Id", function () {
		var policyIdJSON = {"id": "2581f20a-av83-4315-be45-192bc5sEdFff"};
		var deletedPolicyJSON = {};
		httpBackend.when('DELETE', '/policy/2581f20a-av83-4315-be45-192bc5sEdFff').respond(deletedPolicyJSON);

		srv.deletePolicy().delete(policyIdJSON).$promise.then(function(result){
			expect(JSON.stringify(result)).toEqual(JSON.stringify(deletedPolicyJSON));
		});

		rootScope.$digest();
		httpBackend.flush();
	});

	it("Should run a policy by Id", function () {
		var policyIdJSON = {"id": "2581f20a-av83-4315-be45-192bc5sEdFff"};
		var policyRunningJSON = {"message": "test"};
		httpBackend.when('GET', '/policy/run/2581f20a-av83-4315-be45-192bc5sEdFff').respond(policyRunningJSON);

		srv.runPolicy().get(policyIdJSON).$promise.then(function(result){
			expect(JSON.stringify(result)).toEqual(JSON.stringify(policyRunningJSON));
		});

		rootScope.$digest();
		httpBackend.flush();
	});

	it("Should stop a policy", function () {
		var stopPolicyJSON = {"id": "2581f20a-av83-4315-be45-192bc5sEdFff", "status": "Stopping"};
		httpBackend.when('PUT', '/policyContext').respond({});

		srv.stopPolicy().update(stopPolicyJSON).$promise.then(function(response){
			expect(JSON.stringify(response)).toEqual(JSON.stringify({}));
		});

		rootScope.$digest();
		httpBackend.flush();
	});

	it("Should save a policy", function () {
		httpBackend.when('PUT', '/policy').respond(fakePolicyById);

		srv.savePolicy().put().$promise.then(function(response){
			expect(JSON.stringify(response)).toEqual(JSON.stringify(fakePolicyById));
		});

		rootScope.$digest();
		httpBackend.flush();
	});

	it("Should get policies status", function () {
		httpBackend.when('GET', '/policyContext').respond(fakePoliciesStatusList);

		srv.getPoliciesStatus().get().$promise.then(function(result){
			expect(JSON.stringify(result)).toEqual(JSON.stringify(fakePoliciesStatusList));
		});

		rootScope.$digest();
		httpBackend.flush();
	});

  it("Should download a policy by its Id", function () {
  var policyId = '2581f20a-av83-4315-be45-192bc5sEdFff';
    httpBackend.when('GET', '/policy/download/'+ policyId).respond(fakePolicyById);

    srv.downloadPolicy().get({id: policyId}).$promise.then(function(result){
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakePolicyById));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

 });
