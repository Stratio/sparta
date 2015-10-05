describe('filter.truncate-number-filter', function () {
  beforeEach(module('webApp'));

  var filter = null;

  beforeEach(
    inject(function($filter) {
      filter = $filter('truncatenum');
    }));

    it ("if no number is introduced, returns 0", function(){
      expect(filter()).toBe(0);
    });

  it ("if number is introduced, it returns it", function(){
    expect(filter(5)).toBe(5);
    expect(filter(2)).toBe(2);
  })

});
