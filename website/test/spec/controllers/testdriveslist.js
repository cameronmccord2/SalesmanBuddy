'use strict';

describe('Controller: TestdriveslistCtrl', function () {

  // load the controller's module
  beforeEach(module('salesmanBuddyApp'));

  var TestdriveslistCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    TestdriveslistCtrl = $controller('TestdriveslistCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
