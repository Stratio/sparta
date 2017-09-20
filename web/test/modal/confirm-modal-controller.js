describe('modal.confirm-modal-controller', function () {
  beforeEach(module('webApp'));

  var ctrl, modalInstanceMock = null;
  var fakeTitle = "fake modal title";
  var fakeMessage = "This is a fake message in order to be shown in a confirm modal";

  beforeEach(inject(function ($controller) {

    modalInstanceMock = jasmine.createSpyObj('$uibModalInstance', ['close', 'dismiss']);

    ctrl = $controller('ConfirmModalCtrl', {
      '$uibModalInstance': modalInstanceMock,
      'title': fakeTitle,
      'message': fakeMessage
    });
  }));

  it ("should be able to close the modal when user accepts it", function(){
    ctrl.ok();

    expect(modalInstanceMock.close).toHaveBeenCalled();
  });

  it ("should be able to dismiss the modal when user cancel it", function(){
    ctrl.cancel();

    expect(modalInstanceMock.dismiss).toHaveBeenCalledWith('cancel');
  });

});
