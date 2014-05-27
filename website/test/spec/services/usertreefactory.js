'use strict';

describe('Service: userTreeFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var userTreeFactory;
  beforeEach(inject(function (_userTreeFactory_) {
    userTreeFactory = _userTreeFactory_;
  }));

  it('should do something', function () {
    expect(!!userTreeFactory).toBe(true);
  });

});
