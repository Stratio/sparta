describe('policies.factories.request-interceptor-factory', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/cube.json'));
  beforeEach(module('served/policyTemplate.json'));

  var factory,srv, httpBackend, rootScope = null;

  beforeEach(inject(function (_requestInterceptor_,_ApiFragmentService_, $httpBackend, $rootScope) {
    factory = _requestInterceptor_;
    httpBackend = $httpBackend;
    rootScope = $rootScope;
    srv = _ApiFragmentService_;

    httpBackend.when('GET', 'languages/en-US.json').respond({});
  }));

  it("should be able to update web error when a http request fails", function () {
    var fragmentTypeIdJSON = {"type":"input","id":"2581f20a-fd83-4315-be45-192bc5sEdFff"};
    httpBackend.when('GET', '/fragment/input/'+fragmentTypeIdJSON.id).respond(500);
    spyOn(factory, 'responseError').and.callThrough();
    srv.getFragmentById().get(fragmentTypeIdJSON);
    rootScope.$digest();
    httpBackend.flush();
    expect(factory.responseError).toHaveBeenCalled();
    expect(rootScope.error).toBe("_UNAVAILABLE_SERVER_ERROR_");
  });

})
;
