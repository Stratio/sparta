describe('API.template-service', function () {
  beforeEach(module('webApp'));
  beforeEach(module('template/input.json'));
  beforeEach(module('template/output.json'));
  beforeEach(module('template/policy.json'));

  var srv, rootScope, httpBackend, fakeFragmentTemplateByTypeInput, fakeFragmentTemplateByTypeOutput, fakePolicyTemplate = null;

  beforeEach(
    inject(function ($httpBackend, $resource, $rootScope,_templateInput_, _templateOutput_, _templatePolicy_, _ApiTemplateService_) {
      fakeFragmentTemplateByTypeInput = _templateInput_;
      fakeFragmentTemplateByTypeOutput = _templateOutput_;
      fakePolicyTemplate = _templatePolicy_;
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

    rootScope.$digest();
    httpBackend.flush();
  });

});
