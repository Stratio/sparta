describe('service.utils', function () {
  beforeEach(module('webApp'));

  var service = null;

  beforeEach(inject(function (UtilsService) {
    service = UtilsService;

  }));

  describe("should be able to find an element in an JSON array telling him what attribute it has to used to search the element", function () {

    it("if any param is invalid, returns -1", function () {
      var position = service.findElementInJSONArray([], {}, null);
      expect(position).toBe(-1);
      result = service.findElementInJSONArray([], null, "any_attribute");
      expect(position).toBe(-1);
      position = service.findElementInJSONArray(null, {}, "any_attribute");
      expect(position).toBe(-1);
    });

    it("if array is empty, returns -1", function () {
      expect(service.findElementInJSONArray([], {"any_attribute": "fake"}, "any_attribute")).toBe(-1);
    });

    describe("if array is not empty", function () {
      var array = [{"name": "fakename 1"}, {"name": "fakename 2"}, {"name": "fakename 3"}];

      it("if array does not contain the element, returns -1", function () {
        var fakeJson = {"name": "fake value"};

        expect(service.findElementInJSONArray(array, fakeJson, "name")).toBe(-1);
      });

      it("if array contains the element, returns -1", function () {
        var position = 0;
        var fakeJson = {"name": array[position].name};

        expect(service.findElementInJSONArray(array, fakeJson, "name")).toBe(position);

        position = 1;
        fakeJson = {"name": array[position].name};

        expect(service.findElementInJSONArray(array, fakeJson, "name")).toBe(position);
      });

    });

    describe("should be able to remove from an array all elements whose position is contained in the array of positions passed as param", function () {
      it("if array of positions is empty, no element is removed from array", function () {
        var array = [{"any": "fake value"}];
        var positions = [];

        var result = service.removeItemsFromArray(array, positions);
        expect(result).toEqual(array);
      });

      it("if array of positions is not empty, elements whose position is included in the position array are removed from array", function () {
        var array = [{"any": "fake value 1"}, {"any": "fake value 2"}, {"any": "fake value 3"}, {"any": "fake value 4"}];
        var positions = [1, 3];
        var expectedArray = [{"any": "fake value 1"}, {"any": "fake value 3"}];

        var result = service.removeItemsFromArray(array, positions);
        expect(result).toEqual(expectedArray);
      });

      it ("should work correctly although the position array is introduced not sorted", function(){
        var array = [{"any": "fake value 1"}, {"any": "fake value 2"}, {"any": "fake value 3"}, {"any": "fake value 4"}];
        var positions = [3,2];
        var expectedArray = [{"any": "fake value 1"}, {"any": "fake value 2"}];

        var result = service.removeItemsFromArray(array, positions);
        expect(result).toEqual(expectedArray);
      });

    });
  })
});
