describe('policies.wizard.service.model-service', function () {
  beforeEach(module('webApp'));

  var service, ModalMock = null;
  beforeEach(module(function ($provide) {
    ModalMock = jasmine.createSpyObj('$modal', ['open']);

    $provide.value('$modal', ModalMock);
  }));

  beforeEach(inject(function (_ModalService_) {
    service = _ModalService_;
  }));


  it("should be able to call to the angular modal service in order to open a modal with the specified params", function () {
    var fakeController = "fake controller";
    var fakeTemplate = "fake template";
    var fakeResolve = {"fake attribute": "fake value"};

    service.openModal(fakeController, fakeTemplate, fakeResolve);

    var expectedParams = {
      animation: true,
      templateUrl: fakeTemplate,
      controller: fakeController + ' as vm',
      size: 'lg',
      resolve: fakeResolve
    };

    expect(ModalMock.open).toHaveBeenCalledWith(expectedParams);
  });


});
