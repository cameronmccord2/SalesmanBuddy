'use strict';

describe('Service: reportsFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var reportsFactory;
  beforeEach(inject(function (_reportsFactory_) {
    reportsFactory = _reportsFactory_;
  }));

  it('should do something', function () {
    expect(!!reportsFactory).toBe(true);
  });

});
