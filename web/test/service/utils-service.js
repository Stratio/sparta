describe('policies.service.utils-service', function () {
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

    it("should work correctly although the position array is introduced not sorted", function () {
      var array = [{"any": "fake value 1"}, {"any": "fake value 2"}, {"any": "fake value 3"}, {"any": "fake value 4"}];
      var positions = [3, 2];
      var expectedArray = [{"any": "fake value 1"}, {"any": "fake value 2"}];

      var result = service.removeItemsFromArray(array, positions);
      expect(result).toEqual(expectedArray);
    });

  });

  describe("should be able to return a string introduced with an increment number at the end", function () {
    it("if string does not contain a number, it is returned with (2) at the end", function () {
      var string = "fake name";

      expect(service.autoIncrementName(string)).toBe(string + "(2)");
    });

    it("if string contains a number, it is returned with the number plus one at the end", function () {
      var currentNumber = 5;
      var text = "fake name";
      var string = text + "(" + currentNumber + ")";
      expect(service.autoIncrementName(string)).toBe(text + "(" + (currentNumber + 1) + ")");
    });

  });

  describe("should be able to return an array of names from a JSON array with more attributes", function () {
    it("if the introduced array is null, undefined or is empty, it returns an empty array", function () {
      expect(service.getItemNames()).toEqual([]);
      expect(service.getItemNames(null)).toEqual([]);
      expect(service.getItemNames(undefined)).toEqual([]);
      expect(service.getItemNames([])).toEqual([]);
      expect(service.getItemNames(undefined)).toEqual([]);
    });

    it("if the introduced array is valid, it returns an array with names of the element contained in the introduced array with attribute 'name'", function () {
      var fakeJson = {"name": "fake json 1"};
      var invalidJson = {"no_exist_name": "invalid json"};
      var fakeJson2 = {"name": "fake json 2"};

      var array = [fakeJson, invalidJson, fakeJson2];
      expect(service.getItemNames(array).length).toEqual(2);
      expect(service.getItemNames(array)[0]).toEqual(fakeJson.name);
      expect(service.getItemNames(array)[1]).toEqual(fakeJson2.name);
    });

  });

  describe("should be able to return a JSON array of names from an array of json with more attributes", function () {
    it("if the introduced array is null, undefined or is empty, it returns an empty array", function () {
      expect(service.getNamesJSONArray()).toEqual([]);
      expect(service.getNamesJSONArray(null)).toEqual([]);
      expect(service.getNamesJSONArray(undefined)).toEqual([]);
      expect(service.getNamesJSONArray([])).toEqual([]);
      expect(service.getNamesJSONArray(undefined)).toEqual([]);
    });

    it("if the introduced array is valid, it returns a JSON array of names of the elements contained in the introduced array with attribute 'name'", function () {
      var fakeJson = {"name": "fake json 1"};
      var invalidJson = {"no_exist_name": "invalid json"};
      var fakeJson2 = {"name": "fake json 2"};

      var array = [fakeJson, invalidJson, fakeJson2];
      expect(service.getNamesJSONArray(array).length).toEqual(2);
      expect(service.getNamesJSONArray(array)[0]).toEqual(fakeJson);
      expect(service.getNamesJSONArray(array)[1]).toEqual(fakeJson2);
    });

  });

  describe("Should increase the counter of the fragment provided", function () {
    var fakeInputTypeList = null;
    beforeEach(function () {
      fakeInputTypeList = [{'type': 'Kafka', 'count': 2}, {'type': 'Flume', 'count': 1}];
    });

    it("If the input type provided doesn't exist in the inputs list, a new one must be created", function () {
      var fakeInputType = 'Socket';
      service.addFragmentCount(fakeInputTypeList, fakeInputType);
      expect(fakeInputTypeList[2]).toEqual({'type': fakeInputType, 'count': 1});
    });

    it("If the input type provided exists in the inputs list, its counter is increased by one", function () {
      var fakeInputType = 'Flume';
      service.addFragmentCount(fakeInputTypeList, fakeInputType);
      expect(fakeInputTypeList[1]).toEqual({'type': fakeInputType, 'count': 2});
    });
  });

  describe("Should decrease the counter of the fragment provided", function () {
    var fakeInputTypeList, filter = null;
    beforeEach(function () {
      fakeInputTypeList = [{'type': 'Kafka', 'count': 2}, {'type': 'Flume', 'count': 1}];
      filter = {"name": "", "element": {"type": ""}};
    });

    it("If the counter of the input type provided is higher than 1, it is decreased by one", function () {
      var fakeInputType = 'Kafka';
      service.subtractFragmentCount(fakeInputTypeList, fakeInputType, filter);
      expect(fakeInputTypeList[0]).toEqual({'type': fakeInputType, 'count': 1});
    });

    it("If the counter of the input type provided is equal than 1, it is decreased by one and the obejct is deleted", function () {
      var fakeInputType = 'Flume';
      service.subtractFragmentCount(fakeInputTypeList, fakeInputType, filter);
      expect(fakeInputTypeList[1]).toEqual(undefined);
    });
  });

  it("Should be able to convert dotted properties to json properties", function () {
    var inputJson = {
      "writer": {},
      "writer.outputs": ["fake output1", "fake output 2"],
      "reader": {},
      "reader.file": "file1.txt"
    };
    var result = service.convertDottedPropertiesToJson(angular.copy(inputJson));

    expect(result.writer.outputs).toEqual(inputJson["writer.outputs"]);
    expect(result.reader.file).toEqual(inputJson["reader.file"]);
  });

  describe("Should be able to return a string in camel case", function () {
    var inputString = "a_fake_string";
    it("if last param is true, first letter is uppercase", function () {

      var result = service.getInCamelCase(inputString, '_', true);

      expect(result[0]).toBe(inputString[0].toUpperCase());
    });

    it("if last param is false, first letter is lowercase", function () {
      var result = service.getInCamelCase(inputString, '_', false);

      expect(result[0]).toBe(inputString[0].toLowerCase());
    });

    it("should split the string using the separator is introduced as param", function () {
      var result = service.getInCamelCase(inputString, ' ', false);

      expect(result).toBe(inputString);

      var result = service.getInCamelCase(inputString, '_', false);

      expect(result).toBe('aFakeString');
    });

    it("if introduced string or separator are not introduced, string is returned without any change", function () {
      var result = service.getInCamelCase(null, null, false);

      expect(result).toBe(null);

      var result = service.getInCamelCase(inputString, null, false);

      expect(result).toBe(inputString);

      var result = service.getInCamelCase(null, '_', false);

      expect(result).toBe(null);
    })
  });

  describe("it should be able  to generate an option list from an array of strings", function () {

    it("if input array is valid, it converts it into an option list", function() {
      var inputArray = ["option 1", "option 2", "option 3"];
      var expectedOptionList = [{label: inputArray[0], value: inputArray[0]},
        {label: inputArray[1], value: inputArray[1]},
        {label: inputArray[2], value: inputArray[2]}];

      expect(service.generateOptionListFromStringArray(inputArray)).toEqual(expectedOptionList);
    });

    it ("if input array is invalid, it returns an empty array", function(){
      expect(service.generateOptionListFromStringArray()).toEqual([]);
      expect(service.generateOptionListFromStringArray(null)).toEqual([]);
      expect(service.generateOptionListFromStringArray(undefined)).toEqual([]);
      expect(service.generateOptionListFromStringArray([])).toEqual([]);
    });

  })
});
