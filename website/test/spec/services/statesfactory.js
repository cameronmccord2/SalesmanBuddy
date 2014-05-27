'use strict';

describe('Service: statesFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var statesFactory;
  beforeEach(inject(function (_statesFactory_) {
    statesFactory = _statesFactory_;
  }));

  it('should do something', function () {
    expect(!!statesFactory).toBe(true);
  });

});
