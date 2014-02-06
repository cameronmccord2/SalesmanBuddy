var app = angular.module('SALESMANBUDDYADMIN', ['ngRoute']);


app.config(['$routeProvider', function($routeProvider,$locationProvider) {
  $routeProvider.
	when('/commingSoon', { templateUrl: 'templates/comingSoon.html', controller: comingSoonCtrl }).
    otherwise({ redirectTo: '/commingSoon' });
}]);

//******************************************
// Rootscope Setup
//********************************************
app.run(function ($rootScope) {
    
});