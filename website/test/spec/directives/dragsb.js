'use strict';

describe('Directive: dragsb', function () {

  // load the directive's module
  beforeEach(module('tallApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<dragsb></dragsb>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the dragsb directive');
  }));
});
