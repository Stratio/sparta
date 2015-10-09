describe('directive.c-route-button-directive', function () {
  var directive, scope, fakeParams, fakeRoute, fakeUrl, stateMock, locationMock, searchMock = null;
  fakeParams = {"id": "fake Id"};
  fakeRoute = "fake/route";
  fakeUrl = "fake/url";

  beforeEach(module('webApp'));
  beforeEach(module(function ($provide) {
    stateMock = jasmine.createSpyObj("$state", ['go']);

    locationMock = jasmine.createSpyObj("$location", ['path']);
    searchMock = jasmine.createSpy('search');
    locationMock.path.and.callFake(function () {
      return  {"search":searchMock};
    });

    $provide.value('$state', stateMock);
    $provide.value('$location', locationMock);
  }));

  beforeEach(inject(function ($httpBackend, $rootScope) {

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    $httpBackend.when('GET', 'templates/components/c-route-button.tpl.html')
      .respond("<div></div>");

    scope = $rootScope.$new();

    $httpBackend.flush();
  }));

  describe("should redirect to user to the specified route or url and the correct params", function () {

    it("if there is a specified route, user is redirected to that route", inject(function ($compile, $httpBackend) {
      scope.params = fakeParams;
      scope.route = fakeRoute;
      scope.url = null;
      var directive = angular.element('<c-route-button params ="params" route = "route" url = "url"> </c-route-button>');
      directive = $compile(directive)(scope);

      scope.$digest();
      $httpBackend.flush();

      directive.trigger("click");
      expect(stateMock.go).toHaveBeenCalledWith(fakeRoute, fakeParams);
    }));

    it("if there is not a specified route, user is redirected to the specified url", inject(function ($compile, $httpBackend) {
      scope.params = fakeParams;
      scope.route = null;
      scope.url = fakeUrl;
      var directive = angular.element('<c-route-button params ="params" route = "route" url = "url" > </c-route-button>');
      directive = $compile(directive)(scope);

      scope.$digest();
      $httpBackend.flush();

      directive.trigger("click");
      expect(stateMock.go).not.toHaveBeenCalled();
      expect(locationMock.path).toHaveBeenCalledWith(fakeUrl);
      expect(searchMock).toHaveBeenCalledWith(fakeParams);
    }));

    it("if there is not any route or url, user is not redirected",  inject(function ($compile, $httpBackend) {
      scope.params = fakeParams;
      scope.route = null;
      scope.url = null;
      var directive = angular.element('<c-route-button params ="params" route = "route" url = "url" > </c-route-button>');
      directive = $compile(directive)(scope);

      scope.$digest();
      $httpBackend.flush();

      directive.trigger("click");
      expect(stateMock.go).not.toHaveBeenCalled();
      expect(locationMock.path).not.toHaveBeenCalled();
      expect(searchMock).not.toHaveBeenCalled();
    }));
  });

});
