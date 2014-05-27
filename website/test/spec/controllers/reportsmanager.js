'use strict';

describe('Controller: ReportsmanagerCtrl', function () {

  // load the controller's module
  beforeEach(module('salesmanBuddyApp'));

  var ReportsmanagerCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ReportsmanagerCtrl = $controller('ReportsmanagerCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
