describe('API.fragment-service', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/input.json'));
  beforeEach(module('api/inputList.json'));

  var srv, rootScope, httpBackend, fakeInputById, fakeInputListByType = null;

  beforeEach(
    inject(function ($httpBackend, $rootScope, _ApiFragmentService_, _apiInput_, _apiInputList_) {
      fakeInputById 			= _apiInput_;
      fakeInputListByType = _apiInputList_;
      srv 								= _ApiFragmentService_;
      rootScope 					= $rootScope;
      httpBackend 				= $httpBackend;

      httpBackend.when('GET', "languages/en-US.json").respond({});
    })
  );

  it("Should return a fragment by type and Id", function () {
    var fragmentTypeIdJSON = {"type":"input","fragmentId":"2581f20a-fd83-4315-be45-192bc5sEdFff"};
    httpBackend.when('GET', '/fragment/input/id/2581f20a-fd83-4315-be45-192bc5sEdFff').respond(fakeInputById);

    srv.getFragmentById().get(fragmentTypeIdJSON).$promise.then(function(result){
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeInputById));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should return a list of fragments by type", function () {
    var fragmentTypeJSON = {"type":"input"};
    httpBackend.when('GET', '/fragment/input').respond(fakeInputListByType);

    srv.getFragments().get(fragmentTypeJSON).$promise.then(function(result){
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeInputListByType));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should create a fragment", function () {
  	var newFragmentJSON = {"type":"input","id":"2095f20a-fd83-4315-be45-192bc5sEdFff"};
    httpBackend.when('POST', '/fragment').respond(newFragmentJSON);

  	srv.createFragment().create().$promise.then(function(result){
    	expect(JSON.stringify(result)).toEqual(JSON.stringify(newFragmentJSON));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should updatae a fragment", function () {
  	var updatedFragmentJSON = {"type":"output","id":"2581f20a-fd83-4315-be45-192bc5sEdFff"};
    httpBackend.when('PUT', '/fragment').respond(updatedFragmentJSON);

  	srv.updateFragment().update().$promise.then(function(result){
    	expect(JSON.stringify(result)).toEqual(JSON.stringify(updatedFragmentJSON));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should delete a fragment by type and by ID", function () {
  	var fragmentTypeIdJSON = {"type":"output","fragmentId":"2581f20a-fd04-4315-be45-192bc5sEdFff"};
		var deletedFragmentJSON = {};
    httpBackend.when('DELETE', '/fragment/output/id/2581f20a-fd04-4315-be45-192bc5sEdFff').respond(deletedFragmentJSON);

  	srv.deleteFragment().delete(fragmentTypeIdJSON).$promise.then(function(result){
    	expect(JSON.stringify(result)).toEqual(JSON.stringify(deletedFragmentJSON));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

 });
