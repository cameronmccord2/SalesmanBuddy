'use strict';

describe('Service: dealershipsFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var dealershipsFactory;
  beforeEach(inject(function (_dealershipsFactory_) {
    dealershipsFactory = _dealershipsFactory_;
  }));

  it('should do something', function () {
    expect(!!dealershipsFactory).toBe(true);
  });

});
