'use strict';

describe('Service: licensesFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var licensesFactory;
  beforeEach(inject(function (_licensesFactory_) {
    licensesFactory = _licensesFactory_;
  }));

  it('should do something', function () {
    expect(!!licensesFactory).toBe(true);
  });

});
