describe('plugins.controller.plugins-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/pluginList.json'));

  var ctrl, scope, entityFactoryMock, fakePluginList, modalServiceMock, utilsServiceMock;
  var fakePluginName = "plugin";

  //init  mock modules
  beforeEach(inject(function ($controller, $q, $rootScope, _apiPluginList_, $httpBackend) {
    scope = $rootScope.$new();
    fakePluginList = _apiPluginList_;


    entityFactoryMock = jasmine.createSpyObj('EntityFactory', ['getAllPlugins']);
    entityFactoryMock.getAllPlugins.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakePluginList);
      return defer.promise;
    });

    modalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);
    modalServiceMock.openModal.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve();
      return {
        "result": defer.promise
      };
    });

    utilsServiceMock = jasmine.createSpyObj('UtilsService', ['getArrayElementPosition']);

    httpBackend = $httpBackend;
    httpBackend.when('GET', 'languages/en-US.json').respond({});

    ctrl = $controller('PluginsListCtrl', {
      '$scope': scope,
      'EntityFactory': entityFactoryMock,
      'ModalService': modalServiceMock,
      'UtilsService': utilsServiceMock
    });
  }));

  describe("when it is initialized", function () {
    it("should request the current plugin list", function () {
      expect(entityFactoryMock.getAllPlugins).toHaveBeenCalled();
    });

    it("should get plugin list", function () {
      entityFactoryMock.getAllPlugins().then(function (plugins) {
        expect(plugins).toBe(fakePluginList);
      });
    })
  });

  describe("should be able to create a plugin", function () {
    beforeEach(function () {
      ctrl.pluginsData = fakePluginList;
    });

    it("show a creation modal", function () {
      ctrl.createPlugin();
      expect(modalServiceMock.openModal).toHaveBeenCalled();
      var args = modalServiceMock.openModal.calls.mostRecent().args;
      expect(args[0]).toBe('CreateEntityModalCtrl');
    });

    it("should reload plugin list when its one is created", function () {
      ctrl.createPlugin().then(function (plugins) {
        expect(entityFactoryMock.getAllPlugins.calls.count()).toEqual(2);
      });
      scope.$digest();
    });
  });

  describe("should be able to delete a plugin", function () {
    beforeEach(function () {
      ctrl.pluginsData = fakePluginList;
    });

    it("show a delete plugin confirmation", function () {
      ctrl.deletePlugin(fakePluginName);
      expect(modalServiceMock.openModal).toHaveBeenCalled();
      var args = modalServiceMock.openModal.calls.mostRecent().args;
      expect(args[0]).toBe('DeleteEntityModalCtrl');
    })

    it("should delete plugin when its confirmed", function () {
      var currentPluginListLength = ctrl.pluginsData.length;
      ctrl.deletePlugin(fakePluginName).then(function () {
        expect(ctrl.pluginsData.length).toBe(currentPluginListLength - 1);
      });
      scope.$digest();
    });
  });

  it("should be able to order plugin list", function(){
     ctrl.pluginsData = fakePluginList;

     var currentSortField = ctrl.sortField;
     ctrl.sortPlugins("fileName");
     expect(ctrl.tableReverse).toBe(true);
     expect(ctrl.sortField).toBe(currentSortField);

     currentSortField = ctrl.sortField;
     ctrl.sortPlugins("uri");
     expect(ctrl.sortField).not.toBe(currentSortField);
     expect(ctrl.tableReverse).toBe(false);
  });
});
