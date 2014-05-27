'use strict';

describe('Controller: PricingCtrl', function () {

  // load the controller's module
  beforeEach(module('salesmanBuddyApp'));

  var PricingCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    PricingCtrl = $controller('PricingCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
