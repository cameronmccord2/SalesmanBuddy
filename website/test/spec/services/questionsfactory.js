'use strict';

describe('Service: questionsFactory', function () {

  // load the service's module
  beforeEach(module('salesmanBuddyApp'));

  // instantiate service
  var questionsFactory;
  beforeEach(inject(function (_questionsFactory_) {
    questionsFactory = _questionsFactory_;
  }));

  it('should do something', function () {
    expect(!!questionsFactory).toBe(true);
  });

});
