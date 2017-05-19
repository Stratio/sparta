describe('policies.factories.request-interceptor-factory', function () {
  beforeEach(module('webApp'));

  var factory, srv, httpBackend, rootScope, fragmentTypeIdJSON = null;
  var windowObj = {location:{href: ''}};
  var locationObj = {
    protocol: function () {
      return "http"
    }, host: function () {
      return "fakeHost"
    }, port: function () {
      return 8080
    }
  };

  beforeEach(module(function ($provide) {
    $provide.value('$window', windowObj);
    $provide.value('$location', locationObj);
  }));

  beforeEach(inject(function (_requestInterceptor_, _ApiFragmentService_, $httpBackend, $rootScope) {
    factory = _requestInterceptor_;
    httpBackend = $httpBackend;
    rootScope = $rootScope;
    srv = _ApiFragmentService_;

    httpBackend.when('GET', 'languages/en-US.json').respond({});
    fragmentTypeIdJSON = {"type": "input", "fragmentId": "2581f20a-fd83-4315-be45-192bc5sEdFff"};
  }));


  it("should be able to update web error when a http request fails and its code is equal to 0 or 503", function () {

    httpBackend.when('GET', 'fragment/input/id/' + fragmentTypeIdJSON.fragmentId).respond(0);
    spyOn(factory, 'responseError').and.callThrough();
    srv.getFragmentById().get(fragmentTypeIdJSON);
    rootScope.$digest();
    httpBackend.flush();
    expect(factory.responseError).toHaveBeenCalled();
    expect(rootScope.error).toBe("_ERROR_._UNAVAILABLE_SERVER_");

    httpBackend.when('GET', 'fragment/input/id/' + fragmentTypeIdJSON.fragmentId).respond(503);
    srv.getFragmentById().get(fragmentTypeIdJSON);
    rootScope.$digest();
    httpBackend.flush();
    expect(factory.responseError).toHaveBeenCalled();
    expect(rootScope.error).toBe("_ERROR_._UNAVAILABLE_SERVER_");
  });

  it("should be able to redirect to root path when a http request responds with a 401 code", function () {
    httpBackend.when('GET', 'fragment/input/id/' + fragmentTypeIdJSON.fragmentId).respond(401);
    spyOn(factory, 'responseError').and.callThrough();
    srv.getFragmentById().get(fragmentTypeIdJSON);
    rootScope.$digest();
    httpBackend.flush();

    expect(factory.responseError).toHaveBeenCalled();
    var rootPath = locationObj.protocol() + "://" + locationObj.host() + ":" + locationObj.port() + "/";
    expect(windowObj.location.href).toBe(rootPath);
  })
});
