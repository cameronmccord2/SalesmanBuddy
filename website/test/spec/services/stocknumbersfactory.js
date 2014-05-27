'use strict';

describe('Service: stockNumbersFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var stockNumbersFactory;
  beforeEach(inject(function (_stockNumbersFactory_) {
    stockNumbersFactory = _stockNumbersFactory_;
  }));

  it('should do something', function () {
    expect(!!stockNumbersFactory).toBe(true);
  });

});
