'use strict';

describe('Service: genericFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var genericFactory;
  beforeEach(inject(function (_genericFactory_) {
    genericFactory = _genericFactory_;
  }));

  it('should do something', function () {
    expect(!!genericFactory).toBe(true);
  });

});
