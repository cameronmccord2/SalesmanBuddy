'use strict';

describe('Controller: AllusersCtrl', function () {

  // load the controller's module
  beforeEach(module('salesmanBuddyApp'));

  var AllusersCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    AllusersCtrl = $controller('AllusersCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
