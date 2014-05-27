'use strict';

describe('Controller: StocknumbersCtrl', function () {

  // load the controller's module
  beforeEach(module('salesmanBuddyApp'));

  var StocknumbersCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    StocknumbersCtrl = $controller('StocknumbersCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
