'use strict';

angular.module('salesmanBuddyApp')

.constant("baseUrlAWS", "http://salesmanbuddyserver.elasticbeanstalk.com/v1/salesmanbuddy/")
.constant("baseUrlLocal", "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/")
.constant("clientId", "38235450166-qo0e12u92l86qa0h6o93hc2pau6lqkei.apps.googleusercontent.com")
.constant("userInfoEndpoint", "users/me")
.constant("usersPath", "users")
.constant("dealershipsPath", "dealerships")
.constant("statesPath", "states")
.constant("licensesPath", "licenses")
.constant("questionsPath", "questions")
.constant("saveDataPath", "savedata")
.constant("userExistsPath", "userExists")
.constant("licenseImagePath", "licenseimage")
.constant("userTreePath", "userTree")
.constant("errorPath", "error")
.constant("stockNumbersPath", "stockNumbers")
.constant("reportsPath", "reports")
.constant("accessRights", {salesman:1, manager:2, sbUser:3, upperSBUser:4})// page:required type level
.constant("rightsSBUser", 'sbUser')// same as values in accessRights
.constant("rightsManager", 'manager')
.constant("rightsSalesman", 'salesman')
.constant("rightsUpperSBUser", 'upperSBUser')

.config(['$routeProvider', '$locationProvider', 'AuthServiceProvider', function($routeProvider, $locationProvider, AuthServiceProvider) {
$routeProvider.


	// unauthenticated routes

	when('/home', {
		templateUrl: 'views/home.html',
		controller: 'HomeCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/faq', {
		templateUrl: 'views/faq.html',
		controller: 'FaqCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/how', {
		templateUrl: 'views/how.html',
		controller: 'HowCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/pricing', {
		templateUrl: 'views/pricing.html',
		controller: 'PricingCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/loggedOut', {
		templateUrl: 'views/loggedout.html',
		controller: 'LoggedoutCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/contactUs', {
		templateUrl: 'views/contactus.html',
		controller: 'ContactusCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})


	// logged in but no rights

	.when('/newUser/:dealershipCode/:userType', {
		templateUrl: 'views/newuser.html',
		controller: 'NewuserCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})

	// logged in and only a salesman or higher
	.when('/testDrives', {
		templateUrl: 'views/testdriveslist.html',
		controller: 'TestdriveslistCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})


	// sbAdmin routes

	.when('/sbAdmin', {
		redirectTo:'/sbAdmin/dealerships'
	})
	.when('/sbAdmin/reports', {
		templateUrl: 'views/reportsmanager.html',
		controller: 'ReportsmanagerCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/sbAdmin/testDrives', {
		templateUrl: 'views/testdriveslist.html',
		controller: 'TestdriveslistCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/sbAdmin/dealerships', {
		templateUrl: 'views/dealerships.html',
		controller: 'DealershipsCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/sbAdmin/stockNumbers', {
		templateUrl: 'views/stocknumbers.html',
		controller: 'StocknumbersCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/sbAdmin/dashboard', {
		templateUrl: 'views/dashboard.html',
		controller: 'DashboardCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/sbAdmin/users', {
		templateUrl: 'views/allusers.html',
		controller: 'AllusersCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})


	// manager paths

	.when('/manager', {
		redirectTo:'/manager/reports'
	})
	.when('/manager/stockNumbers', {
		templateUrl: 'views/stocknumbers.html',
		controller: 'StocknumbersCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/manager/reports', {
		templateUrl: 'views/reportsmanager.html',
		controller: 'ReportsmanagerCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	.when('/manager/dashboard', {
		templateUrl: 'views/dashboard.html',
		controller: 'DashboardCtrl',
		resolve: AuthServiceProvider.waitForLogin
	})
	// .when('/manager/users', {
	// 	templateUrl: 'views/allusers.html',
	// 	controller: 'AllusersCtrl',
	// 	resolve: AuthServiceProvider.waitForLogin
	// })
	// .when('/manager/testDrives', {
	// 	templateUrl: 'views/testdriveslist.html',
	// 	controller: 'TestdriveslistCtrl',
	// 	resolve: AuthServiceProvider.waitForLogin
	// })
	.otherwise({ redirectTo: '/home' });
}])

.config(['AuthServiceProvider', 'clientId', 'baseUrlAWS', 'baseUrlLocal', function(AuthServiceProvider, clientId, baseUrlAWS, baseUrlLocal){
	function determineBaseUrl() {// allows us to not have to change the client id every time we move environments
		if(window.location.host == 'localhost:8080' || window.location.host == 'localhost:9000')
			return baseUrlLocal;
		else if(window.location.host == 'salesmanbuddy.com')
			return baseUrlAWS;

		throw new Error("App is running in an unknown environment. Please check URL in browser");
	}
		AuthServiceProvider.setRefreshUrl(determineBaseUrl() + 'refreshToken');
		AuthServiceProvider.setCodeForTokenUrl(determineBaseUrl() + 'codeForToken');
		AuthServiceProvider.setClientID(clientId);
		AuthServiceProvider.pushScope('https://www.googleapis.com/auth/plus.me');
		AuthServiceProvider.pushScope('email');
		AuthServiceProvider.pushScope('profile');
		// AuthServiceProvider.setRedirectURI('http://localhost:8080/salesmanBuddyAdmin');

		// this only supports matching the first part of the path without any /
		AuthServiceProvider.pushNonAuthenticatedPath("home");
		AuthServiceProvider.pushNonAuthenticatedPath("faq");
		AuthServiceProvider.pushNonAuthenticatedPath("contactUs");
		AuthServiceProvider.pushNonAuthenticatedPath("loggedOut");
		AuthServiceProvider.pushNonAuthenticatedPath("pricing");
		AuthServiceProvider.pushNonAuthenticatedPath("how");
}])

.config(['$sceProvider', function($sceProvider) {
	// Completely disable SCE.  For demonstration purposes only!
	// Do not use in new projects.
	$sceProvider.enabled(false);
}]);













































