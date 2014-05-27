'use strict';

describe('Service: licenseImageFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var licenseImageFactory;
  beforeEach(inject(function (_licenseImageFactory_) {
    licenseImageFactory = _licenseImageFactory_;
  }));

  it('should do something', function () {
    expect(!!licenseImageFactory).toBe(true);
  });

});
