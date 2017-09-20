describe('drivers.controller.drivers-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/driverList.json'));

  var ctrl, scope, entityFactoryMock, fakeDriverList, modalServiceMock, utilsServiceMock;
  var fakeDriverName = "driver";

  //init  mock modules
  beforeEach(inject(function ($controller, $q, $rootScope, _apiDriverList_, $httpBackend) {
    scope = $rootScope.$new();
    fakeDriverList = _apiDriverList_;


    entityFactoryMock = jasmine.createSpyObj('EntityFactory', ['getAllDrivers']);
    entityFactoryMock.getAllDrivers.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakeDriverList);
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

    ctrl = $controller('DriversListCtrl', {
      '$scope': scope,
      'EntityFactory': entityFactoryMock,
      'ModalService': modalServiceMock,
      'UtilsService': utilsServiceMock
    });
  }));

  describe("when it is initialized", function () {
    it("should request the current driver list", function () {
      expect(entityFactoryMock.getAllDrivers).toHaveBeenCalled();
    });

    it("should get driver list", function () {
      entityFactoryMock.getAllDrivers().then(function (drivers) {
        expect(drivers).toBe(fakeDriverList);
      });
    })
  });

  describe("should be able to create a driver", function () {
    beforeEach(function () {
      ctrl.driversData = fakeDriverList;
    });

    it("show a creation modal", function () {
      ctrl.createDriver();
      expect(modalServiceMock.openModal).toHaveBeenCalled();
      var args = modalServiceMock.openModal.calls.mostRecent().args;
      expect(args[0]).toBe('CreateEntityModalCtrl');
    });

    it("should reload driver list when its one is created", function () {
      ctrl.createDriver().then(function (drivers) {
        expect(entityFactoryMock.getAllDrivers.calls.count()).toEqual(2);
      });
      scope.$digest();
    });
  });

  describe("should be able to delete a driver", function () {
    beforeEach(function () {
      ctrl.driversData = fakeDriverList;
    });

    it("show a delete driver confirmation", function () {
      ctrl.deleteDriver(fakeDriverName);
      expect(modalServiceMock.openModal).toHaveBeenCalled();
      var args = modalServiceMock.openModal.calls.mostRecent().args;
      expect(args[0]).toBe('DeleteEntityModalCtrl');
    })

    it("should delete driver when its confirmed", function () {
      var currentDriverListLength = ctrl.driversData.length;
      ctrl.deleteDriver(fakeDriverName).then(function () {
        expect(ctrl.driversData.length).toBe(currentDriverListLength - 1);
      });
      scope.$digest();
    });
  });

  it("should be able to order driver list", function(){
     ctrl.driversData = fakeDriverList;

     var currentSortField = ctrl.sortField;
     ctrl.sortDrivers("fileName");
     expect(ctrl.tableReverse).toBe(true);
     expect(ctrl.sortField).toBe(currentSortField);

     currentSortField = ctrl.sortField;
     ctrl.sortDrivers("uri");
     expect(ctrl.sortField).not.toBe(currentSortField);
     expect(ctrl.tableReverse).toBe(false);
  });
});
