'use strict';

describe('Controller: LoggedoutCtrl', function () {

  // load the controller's module
  beforeEach(module('salesmanBuddyApp'));

  var LoggedoutCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    LoggedoutCtrl = $controller('LoggedoutCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
