var app = angular.module('SALESMANBUDDYADMIN', ['ngRoute', 'AuthenticationService']);

// app.constant("baseUrl", "http://salesmanbuddyserver.elasticbeanstalk.com/v1/salesmanbuddy/");
app.constant("baseUrl", "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/");
app.value("userInfoEndpoint", "users/me");
app.value("usersPath", "users");
app.value("dealershipsPath", "dealerships");
app.value("statesPath", "states");
app.value("licensesPath", "licenses");
app.value("questionsPath", "questions");
app.value("saveDataPath", "savedata");
app.value("userExistsPath", "userExists");
app.value("licenseImagePath", "licenseimage");

app.config(['$routeProvider', '$locationProvider', 'AuthServiceProvider', function($routeProvider, $locationProvider, AuthServiceProvider) {
  $routeProvider.
	when('/comingSoon', { templateUrl: 'templates/comingSoon.html', controller: comingSoonCtrl, resolve: AuthServiceProvider.waitForLogin}).
	when('/home', { templateUrl: 'templates/home.html', controller: homeCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/faq', { templateUrl: 'templates/help.html', controller: helpCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/loggedOut', { templateUrl: 'templates/loggedOut.html', controller: loggedOutCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/contactUs', { templateUrl: 'templates/contactUs.html', controller: contactUsCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/allUsers', { templateUrl: 'templates/allUsers.html', controller: allUsersCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/licensesList', { templateUrl: 'templates/licensesList.html', controller: licensesListCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/dealershipManager', { templateUrl: 'templates/dealershipManager.html', controller: dealershipManagerCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/newUser/:dealershipCode', { templateUrl: 'templates/newUser.html', controller: newUserCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/pricing', {templateUrl: 'templates/pricing.html', resolve: AuthServiceProvider.waitForLogin}).
	when('/how', {templateUrl: 'templates/how.html', resolve: AuthServiceProvider.waitForLogin}).
	// when('/loggingIn/:whereTo', {templateUrl:'templates/loggingIn.html', controller: loggingInCtrl, resolve: AuthServiceProvider.waitForLogin }).
	otherwise({ redirectTo: '/comingSoon' });
}]);

app.config(['AuthServiceProvider', 'baseUrl', function(AuthServiceProvider, baseUrl){
		AuthServiceProvider.setRefreshUrl(baseUrl + 'refreshToken');
		AuthServiceProvider.setServerUrl(baseUrl + 'codeForToken');
		AuthServiceProvider.setClientID('38235450166-qo0e12u92l86qa0h6o93hc2pau6lqkei.apps.googleusercontent.com');
		AuthServiceProvider.pushScope('https://www.googleapis.com/auth/plus.me');
		AuthServiceProvider.pushScope('email');
		AuthServiceProvider.pushScope('profile');
		// AuthServiceProvider.setRedirectURI('http://localhost:8080/salesmanBuddyAdmin');

		// this only supports matching the first part of the path without any /
		AuthServiceProvider.pushNonAuthenticatedPath("comingSoon");
		AuthServiceProvider.pushNonAuthenticatedPath("home");
		AuthServiceProvider.pushNonAuthenticatedPath("faq");
		AuthServiceProvider.pushNonAuthenticatedPath("contactUs");
		AuthServiceProvider.pushNonAuthenticatedPath("loggedOut");
		AuthServiceProvider.pushNonAuthenticatedPath("pricing");
		AuthServiceProvider.pushNonAuthenticatedPath("how");
}]);

app.config(function($sceProvider) {
	// Completely disable SCE.  For demonstration purposes only!
	// Do not use in new projects.
	$sceProvider.enabled(false);
});


app.factory('genericFactory', function($http, $q){
	var factory = {};
	factory.request = function(verb, url, errorMessage, object, options){
		var defer = $q.defer();
		verb = verb.toLowerCase();

		if(verb == 'get' || verb == 'delete' || verb == 'head' || verb == 'jsonp'){
			$http[verb](url, options).success(function(data){
				defer.resolve(data);
			}).error(function(data, status, headers, config){
				console.log(errorMessage || "", data, status, headers, config);
				defer.reject(data);
			});
			return defer.promise;

		}else if(verb == 'put' || verb == 'post'){
			
			$http[verb](url, object, options).success(function(data){
				defer.resolve(data);
			}).error(function(data, status, headers, config){
				console.log(errorMessage || "", data, status, headers, config);
				defer.reject(data);
			});
			return defer.promise;
		}
		else
			console.log("you need to specify a valid restful verb");
	}
	return factory;
});

// app.factory('genericFactory', ['$http', '$q', 'User', 'mtc.api.config', function($http, $q, User, apiconfig){
// 	var factory = {};
// 	factory.request = function(verb, url, errorMessage, object, options){
// 		var defer = $q.defer();
// 		verb = verb.toLowerCase();

// 		if(verb == 'get' || verb == 'delete' || verb == 'head' || verb == 'jsonp'){
// 			$http[verb](url, options).success(function(data){
// 				defer.resolve(data);
// 			}).error(function(data, status, headers, config){
// 				console.log(errorMessage || "", data, status, headers, config);
// 				defer.reject(data);
// 			});
// 			return defer.promise;

// 		}else if(verb == 'put' || verb == 'post'){
			
// 			$http[verb](url, object, options).success(function(data){
// 				defer.resolve(data);
// 			}).error(function(data, status, headers, config){
// 				console.log(errorMessage || "", data, status, headers, config);
// 				defer.reject(data);
// 			});
// 			return defer.promise;
// 		}
// 		else
// 			console.log("you need to specify a valid restful verb");
// 	}

// 	factory.getIndicesOf = function(searchStr, str, caseSensitive) {
// 	    var startIndex = 0, searchStrLen = searchStr.length;
// 	    var index, indices = [];
// 	    if (!caseSensitive) {
// 	        str = str.toLowerCase();
// 	        searchStr = searchStr.toLowerCase();
// 	    }
// 	    while ((index = str.indexOf(searchStr, startIndex)) > -1) {
// 	        indices.push(index);
// 	        startIndex = index + searchStrLen;
// 	    }
// 	    return indices;
// 	}

// 	factory.allPromisesRequest = function(promises, verb, url, errorMessage, object, options){
// 		var defer = $q.defer();

// 		$q.all(promises).then(function(resolutions) {

// 			var variables = url.match(/:[^/]*;/g);// :api;/users/:user;/:users; - would find 3 variables to replace: api, user, users
// 			for (var i = variables.length - 1; i >= 0; i--) {

// 				var resolutionVariable = variables[i].split(":")[1].split(";")[0];// strip off : and ;
// 				var replaceString = variables[i];
// 				var indicies = factory.getIndicesOf(replaceString, url, true);

// 				for (var j = indicies.length - 1; j >= 0; j--) {// number of replacements need to do
// 					url = url.replace(replaceString, resolutions[resolutionVariable]);
// 				};
// 			};
			
// 			factory.request(verb, url, errorMessage).then(function(data){
// 				defer.resolve(data);
// 			}, function(data){
// 				defer.reject(data);// the error has already been logged to the console so no need to do it here too, just pass on the reject if they want it
// 			});
// 		}, function(data){
// 			console.log("the $q.all(promises) responded with a reject, somewhere your promises rejected somehow, you specified error message: " + errorMessage);
// 			defer.reject(data);
// 		});
// 		return defer.promise;
// 	}

// 	factory.getUserId = function(){
// 		var defer = $q.defer();
// 		User.initUser().then(function(){
// 			defer.resolve(User.getUser().user.id);
// 		}, function(){
// 			defer.reject('init user failed');
// 		});
// 		return defer.promise;
// 	}

// 	factory.apiPromiseForKey = function(key){
// 		return {
// 			'api':apiconfig.getPrefix(key)
// 		};
// 	}
// 	return factory;
// }]);

app.factory('usersFactory',function(baseUrl, usersPath, genericFactory, $http, $q, $window){
	var factory = {};

	factory.userExists = function(){
		console.log("running user extists")

		var defer = $q.defer();

		var user = {
			deviceType:2,//1:ios, 2:web, 3:android, this is used to know what client the refresh token is associated with
			// type:1, hard coded in the server to give them 1, that can be changed in the all users view in the admin
			refreshToken: $window.sessionStorage.refreshToken || ""
		};
		if(user.refreshToken && user.refreshToken.length > 0){
			$http.put(baseUrl + usersPath + '/userExists', user, {cache:true}).success(function(data){
				defer.resolve(data);
			}).error(function(data, status, headers, config){
				console.log('error userExists', data, status, headers, config);
				defer.reject(data);
			});
		}else{
			defer.reject();
			console.log("refresh token wasnt on the object")
		}
		return defer.promise;
	}

	factory.getAllUsers = function(){
		// factory.userExists();
		return genericFactory.request('get', baseUrl + usersPath, "error getAllUsers");
	}

	factory.updateUserToType = function(googleUserId, type){
		// factory.userExists();
		var options = {
			params:{
				type:type
			}
		};
		return genericFactory.request('post', baseUrl + usersPath + "/" + googleUserId, "error updateUserToType", null, options);
	}

	factory.updateUserToDealershipCode = function(googleUserId, dealershipCode){
		// factory.userExists();
		var options = {
			params:{
				dealershipcode:dealershipCode
			}
		};
		return genericFactory.request('post', baseUrl + usersPath + "/" + googleUserId, "error updateUserToDealershipCode", undefined, options);
	}

	factory.getGoogleUserObject = function(){
		// factory.userExists();
		// return genericFactory.request('get', baseUrl + usersPath + "/me", "error getGoogleUserObject", {cache:true});
		return genericFactory.request('get', 'https://www.googleapis.com/oauth2/v1/userinfo?alt=json', 'error getUserObject', {cache:true});
	}

	factory.getNameForUser = function(googleUserId){
		return genericFactory.request('get', baseUrl + usersPath + "/" + googleUserId + "/google/name", undefined, {cache:true})
	}

	return factory;
});

app.factory('dealershipsFactory',function(baseUrl, dealershipsPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllDealerships = function(){
		// usersFactory.userExists();
		return genericFactory.request('get', baseUrl + dealershipsPath, "error getAllDealerships");
	}

	factory.newDealership = function(dealership){
		// usersFactory.userExists();
		return genericFactory.request('put', baseUrl + dealershipsPath, "error newDealership", dealership);
	}

	factory.updateDealership = function(dealership){
		// usersFactory.userExists();
		return genericFactory.request('post', baseUrl + dealershipsPath, "error updateDealership", dealership);
	}

	return factory;
});

app.factory('statesFactory',function(baseUrl, statesPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllStates = function(){
		// usersFactory.userExists();
		return genericFactory.request('get', baseUrl + statesPath, "error getAllStates");
	}

	return factory;
});

app.factory('licensesFactory',function(baseUrl, licensesPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllLicensesForUser = function(){
		// usersFactory.userExists();
		return genericFactory.request('get', baseUrl + licensesPath, "error getAllLicensesForUser");
	}

	factory.getAllLicensesForDealership = function(){
		// usersFactory.userExists();
		var options = {
			params:{
				dealership:true
			}
		};
		return genericFactory.request('get', baseUrl + licensesPath, "error getAllLicensesForDealership", undefined, options);
	}

	factory.putNewLicense = function(license){
		// not implemented for admin
	}

	factory.updateLicense = function(license){
		// not implemented for admin
	}

	factory.deleteLicense = function(licenseId){
		// not implemented for admin
	}

	return factory;
});

app.factory('questionsFactory',function(baseUrl, questionsPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllQuestions = function(){
		// usersFactory.userExists();
		return genericFactory.request('get', baseUrl + questionsPath, "error getAllQuestions");
	}

	factory.putNewQuestion = function(question){
		// not implemented in admin
	}

	factory.updateQuestion = function(question){
		// not implemented in admin
	}

	return factory;
});

app.factory('licenseImageFactory',function(baseUrl, licenseImagePath, saveDataPath, genericFactory, usersFactory){
	var factory = {};

	factory.getLicenseImageForAnswerId = function(answerId){
		// usersFactory.userExists();
		var options = {
			params:{
				answerid:answerId
			}
		};
		return genericFactory.request('get', baseUrl + licenseImagePath, "error getLicenseImageForAnswerId");
	}

	factory.saveLicenseImage = function(){
		// not implemented in admin
	}

	return factory;
});

//******************************************
// Rootscope Setup
//********************************************
app.run(function ($rootScope, $http, User, AuthService, $location, usersFactory, $q, userInfoEndpoint, baseUrl, dealershipsFactory) {
	User.setUserInfoEndpoint(baseUrl + userInfoEndpoint);
	$http.defaults.headers.common.authprovider = "google";

	$rootScope.needsToBeLoggedIn;
	$rootScope.userIsLoggedIn = false;
	$rootScope.user = null;

	$rootScope.logout = function(){
		$rootScope.user = null;
		User.logout();
	}

	$rootScope.goToPage = function(page){
		$location.path("/" + page);
	}

	if(AuthService.isTokenValid()){
		User.initUser().then(function(data){
			$rootScope.user = data;
			console.log(data)
		});
		// usersFactory.getGoogleUserObject().then(function(user){
		// 	$rootScope.user = user;
		// });
	}

	$rootScope.testRequest = function(){
		dealershipsFactory.getAllDealerships().then(function(data){
			console.log(data);
		});
	}

	$rootScope.doesUserHaveAccessTo = function(what){
		if($rootScope.user){
			var type = $rootScope.user.sb.type;
			if(what == 'licensesList' && type > 1)
				return true;
			if(what == 'allUsers' && type > 2)
				return true;
			if(what == 'dealershipManager' && type > 2)
				return true;
		}
		return false;
	}

	$rootScope.isPath = function(path){
		return $rootScope.isSelected(path).length;
	}

	$rootScope.isSelected = function(path, div){
		var currentPath = $location.path().substr(1, 1+path.length);
		if(div){
			if(currentPath == path)
				return 'navDivSelected';
		}else{
			if(currentPath == path)
				return 'navTextSelected';
		}
		return '';
	}
});




















