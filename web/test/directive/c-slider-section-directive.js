describe('directive.c-slider-section-directive', function () {
  var directive, scope, $window, fakeHiddenSectionHeight = null;
  beforeEach(module('webApp'));
  beforeEach(inject(function ($httpBackend, $rootScope, $compile, _$window_) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    scope = $rootScope.$new();
    fakeHiddenSectionHeight = 200;
    $window = _$window_;

    directive = angular.element('<c-slider-section data-hidden-section-id = "hidden-section"></c-slider-section>');
    directive = $compile(directive)(scope);
    $(document.body).append(directive);
    scope.$digest();

    var hiddenSection = angular.element('<div id = "hidden-section"></div>');
    hiddenSection.height(fakeHiddenSectionHeight);

    angular.element(directive).append(hiddenSection);
  }));

  it("should be able to change the visibility of the hidden section", function () {
    scope.showHiddenSection = false;

    scope.changeHiddenSectionVisibility();

    expect(scope.showHiddenSection).toBeTruthy();

    scope.changeHiddenSectionVisibility();

    expect(scope.showHiddenSection).toBeFalsy();
  });

  describe("should be able to hide or show slider button according to scroll", function () {
    it("if hidden section is visible and scroll is major than its height, slider button is hidden", function () {
      scope.showHiddenSection = true;
      window.scrollY = fakeHiddenSectionHeight + 1;
      $window.onscroll();

      expect(scope.showSliderSectionButton).toBeFalsy();
    });

    it("if hidden section is visible and scroll is minor than its height, slider button is visible", function () {
      scope.showHiddenSection = true;
      window.scrollY = fakeHiddenSectionHeight - 1;
      $window.onscroll();

      expect(scope.showSliderSectionButton).toBeTruthy();
    });

    it("if hidden section is not visible and scroll is major than 1, slider button is hidden", function () {
      scope.showHiddenSection = false;
      window.scrollY = 2;
      $window.onscroll();

      expect(scope.showSliderSectionButton).toBeFalsy();
    });

    it("if hidden section is not visible and scroll is minor than 1, slider button is visible", function () {
      scope.showHiddenSection = false;
      window.scrollY =  0;
      $window.onscroll();

      expect(scope.showSliderSectionButton).toBeTruthy();
    });

  })
});
