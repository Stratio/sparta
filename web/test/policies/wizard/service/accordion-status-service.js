describe('service.accordionStatusService', function () {
  beforeEach(module('webApp'));

  var service = null;


  beforeEach(inject(function (_AccordionStatusService_) {
    service = _AccordionStatusService_;

  }));

  describe("should be able to reset the accordion status", function () {
    it("if the first param is null or undefined, accordion status is not reset", function () {
      var length = null;
      service.resetAccordionStatus(length);

      expect(service.getAccordionStatus()).toEqual([]);

      length = undefined;
      service.resetAccordionStatus(length);

      expect(service.getAccordionStatus()).toEqual([]);
    });

    it("if the first param is a valid length, accordion status is reset with the that length more one position", function () {
      var length = 5;
      service.resetAccordionStatus(length);

      expect(service.getAccordionStatus().length).toEqual(length + 1);
    });

    it("if the second param is introduced, that param is used to specify the position that has to be true", function () {
      var length = 5;
      var truePosition = 4;
      service.resetAccordionStatus(length, truePosition);

      expect(service.getAccordionStatus().length).toEqual(length + 1);
      for (var i = 0; i <= length; ++i) {
        if (i != truePosition)
          expect(service.getAccordionStatus()[i]).toEqual(false);
      }
      expect(service.getAccordionStatus()[4]).toEqual(true);
    });

    it("if the second param is not introduced, the last position of the accordion status array is set to true", function () {
      var length = 5;
      service.resetAccordionStatus(length);

      expect(service.getAccordionStatus().length).toEqual(length + 1);
      for (var i = 0; i < length; ++i) {
        expect(service.getAccordionStatus()[i]).toEqual(false);
      }
      expect(service.getAccordionStatus()[length]).toEqual(true);
    })
  })

});
