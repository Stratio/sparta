describe('API Template service', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/inputTemplate.json'));
  beforeEach(module('served/outputTemplate.json'));
  beforeEach(module('served/policyTemplate.json'));

  var srv, rootScope, httpBackend, fakeFragmentTemplateByTypeInput, fakeFragmentTemplateByTypeOutput, fakePolicyTemplate = null;

  // init mock modules
  var apiConfigSettingsMock = jasmine.createSpyObj('apiConfigSettings',['timeout']);

  beforeEach(
    inject(function ($httpBackend, $resource, $rootScope,_servedInputTemplate_, _servedOutputTemplate_, _servedPolicyTemplate_, _ApiTemplateService_) {
      fakeFragmentTemplateByTypeInput = _servedInputTemplate_;
      fakeFragmentTemplateByTypeOutput = _servedOutputTemplate_;
      fakePolicyTemplate = _servedPolicyTemplate_;
      rootScope = $rootScope;
      srv = _ApiTemplateService_;
      httpBackend = $httpBackend;

      httpBackend.when('GET', 'languages/en-US.json').respond({});
    })
  );

  it("Should return input templates", function () {
    var fragmentTypeJSON = {"type":"input.json"};
    httpBackend.when('GET', '/data-templates/input.json').respond(fakeFragmentTemplateByTypeInput);

    srv.getFragmentTemplateByType().get(fragmentTypeJSON).$promise.then(function(result){
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeFragmentTemplateByTypeInput));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should return output templates", function () {
    var fragmentTypeJSON = {"type":"output.json"};
    httpBackend.when('GET', '/data-templates/output.json').respond(fakeFragmentTemplateByTypeOutput);

    srv.getFragmentTemplateByType().get(fragmentTypeJSON).$promise.then(function(result) {
     expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeFragmentTemplateByTypeOutput));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should return policy templates", function () {
   httpBackend.when('GET', '/data-templates/policy.json').respond(fakePolicyTemplate);

    srv.getPolicyTemplate().get().$promise.then(function(result){
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakePolicyTemplate));
    });

    rootScope.$digest;
    httpBackend.flush();
  });

});
