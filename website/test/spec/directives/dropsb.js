'use strict';

describe('Directive: dropsb', function () {

  // load the directive's module
  beforeEach(module('tallApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<dropsb></dropsb>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the dropsb directive');
  }));
});
