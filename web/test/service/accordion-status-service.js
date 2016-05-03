describe('policies.service.accordion-status-service', function () {
  beforeEach(module('webApp'));

  var service = null;


  beforeEach(inject(function (_AccordionStatusService_) {
    service = _AccordionStatusService_;

  }));

  describe("should be able to reset the accordion status", function () {
    var accordionStatus = [false, true, false];
    it("if length param is null or undefined, accordion status is not reset", function () {
      var length = null;

      var resetAccordionStatus = service.resetAccordionStatus(angular.copy(accordionStatus), length);

      expect(resetAccordionStatus).toEqual(accordionStatus);

      length = undefined;
      resetAccordionStatus = service.resetAccordionStatus(angular.copy(accordionStatus), length);

      expect(resetAccordionStatus).toEqual(accordionStatus);
    });

    it("if the length param is valid, accordion status is reset with the that length more one position", function () {
      var length = 5;
      var resetAccordionStatus = service.resetAccordionStatus(angular.copy(accordionStatus), length);

      expect(resetAccordionStatus.length).toEqual(length + 1);
    });

    it("if the third param is introduced, that param is used to specify the position that has to be true", function () {
      var length = 5;
      var truePosition = 4;
      var resetAccordionStatus = service.resetAccordionStatus(angular.copy(accordionStatus), length, truePosition);

      expect(resetAccordionStatus.length).toEqual(length + 1);
      for (var i = 0; i <= length; ++i) {
        if (i != truePosition)
          expect(resetAccordionStatus[i]).toEqual(false);
      }
      expect(resetAccordionStatus[4]).toEqual(true);
    });

    it("accordion status length is adjusted to the new length if the new length is minor than accordion status one", function () {
      var oldLength = 5;
      var newLength = 3;
      service.resetAccordionStatus(angular.copy(accordionStatus), oldLength);
      var resetAccordionStatus = service.resetAccordionStatus(angular.copy(accordionStatus), newLength);

      expect(resetAccordionStatus.length).toBe(newLength + 1);
    });

    it("if length is minor thant current length, last positions are removing until to have the same length", function () {
      var newLength = 1;

      var resetAccordionStatus = service.resetAccordionStatus(angular.copy(accordionStatus), newLength);

      expect(resetAccordionStatus.length).toBe(newLength + 1);
    });
  })

});
