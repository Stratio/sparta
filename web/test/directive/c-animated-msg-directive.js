describe('directive.c-animated-msg-directive', function () {
  beforeEach(module('webApp'));
  var directive, scope, $timeout, fakeMsg, isolatedScope = null;

  beforeEach(inject(function ($httpBackend, $rootScope, $compile, _$timeout_) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});
    $httpBackend.when('GET', 'templates/components/c-animated-msg.tpl.html')
      .respond("<div></div>");

    scope = $rootScope.$new();
    $timeout = _$timeout_;
    fakeMsg = {text: ""};
    scope.msg = fakeMsg;
    scope.msgType = 'error';
    directive = angular.element('<c-animated-msg items="items" msg = "msg" type = msgType></c-animated-msg>');
    directive = $compile(directive)(scope);
    scope.$digest();
    $httpBackend.flush();

    isolatedScope = directive.isolateScope();
  }));

  describe("Should reset timer to hide the error when error is modified", function () {
    it("if timeout is not defined, error is never cleaned", function () {
      isolatedScope.timeout = null;
      fakeMsg.text = "fake error message";
      scope.$apply();
      $timeout.flush(10000);
      expect(isolatedScope.msg.text).not.toBe('');
    });

    it("if timeout is defined, error is cleaned after this time", function () {
      var fakeTimeout = 6000;
      isolatedScope.timeout = fakeTimeout;
      fakeMsg.text = "fake error message";
      scope.$apply();

      $timeout.flush(fakeTimeout-1);
      expect(isolatedScope.msg.text).toBe(fakeMsg.text); // it is not cleaned yet

      $timeout.flush(1);
      expect(isolatedScope.msg.text).toBe('');  // it is cleaned
    });

    it ("timer is canceled when directive is removed", function(){
      spyOn($timeout, 'cancel');
      isolatedScope.$destroy();

      scope.$apply();

      expect($timeout.cancel).toHaveBeenCalled();
    })
  });
});
