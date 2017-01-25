describe('policies.wizard.controller.delete-policy-modal-controller', function () {
  beforeEach(module('webApp'));

  var ctrl, policyFactoryMock, modalInstanceMock, fakePolicy, resolvedPromise, rejectedPromise, scope, fakeError = null;

  beforeEach(inject(function ($controller, $q, $rootScope, $httpBackend) {
    scope = $rootScope.$new();
    resolvedPromise = function () {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };
    fakeError = {"data": {"i18nCode": "fake error message"}};
    rejectedPromise = function () {
      var defer = $q.defer();
      defer.reject(fakeError);

      return defer.promise;
    };
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});
    policyFactoryMock = jasmine.createSpyObj('PolicyFactory', ['deletePolicy']);
    policyFactoryMock.deletePolicy.and.callFake(resolvedPromise);

    modalInstanceMock = jasmine.createSpyObj('$uibModalInstance', ['close', 'dismiss']);
    fakePolicy = {"id": "fake policy id"};

    ctrl = $controller('DeletePolicyModalCtrl', {
      '$uibModalInstance': modalInstanceMock,
      'item': fakePolicy,
      'PolicyFactory': policyFactoryMock
    });


  }));

  it("when it is initialized, it saves the introduced item as a policyData variable", function () {
    expect(ctrl.policyData).toBe(fakePolicy);
  });

  describe("should be able to confirm the modal", function () {

    it("should remove the policy introduced when modal is confirmed by user", function () {
      ctrl.ok();

      expect(policyFactoryMock.deletePolicy).toHaveBeenCalledWith(fakePolicy.id);
    });

    it("should close modal when policy has been removed successfully", function () {
      policyFactoryMock.deletePolicy.and.callFake(resolvedPromise);
      ctrl.ok().then(function () {
        expect(modalInstanceMock.close).toHaveBeenCalledWith(fakePolicy);
      });
      scope.$digest();
    });

    it("should show a error (formatted as a language key) when policy has not been removed successfully", function () {
      policyFactoryMock.deletePolicy.and.callFake(rejectedPromise);
      ctrl.ok().then(function () {
        expect(ctrl.error).toBeTruthy();
        expect(ctrl.errorText).toBe("_ERROR_._" + fakeError.data.i18nCode + "_");
      });
      scope.$digest();
    });
  });

  it ("should dismiss the modal when user cancels it", function(){
    ctrl.cancel();
    expect(modalInstanceMock.dismiss).toHaveBeenCalledWith('cancel');
  })
});
