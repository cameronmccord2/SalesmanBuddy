'use strict';

describe('Controller: DealershipsCtrl', function () {

  // load the controller's module
  beforeEach(module('salesmanBuddyApp'));

  var DealershipsCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    DealershipsCtrl = $controller('DealershipsCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
