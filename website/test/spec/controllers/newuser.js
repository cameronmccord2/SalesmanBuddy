'use strict';

describe('Controller: NewuserCtrl', function () {

  // load the controller's module
  beforeEach(module('salesmanBuddyApp'));

  var NewuserCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    NewuserCtrl = $controller('NewuserCtrl', {
      $scope: scope
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(scope.awesomeThings.length).toBe(3);
  });
});
