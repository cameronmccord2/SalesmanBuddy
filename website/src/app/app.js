var app = angular.module('SALESMANBUDDYADMIN', ['ngRoute', 'AuthenticationService']);

// TODO new user managers don't get assigned to type 2

// app.constant("baseUrl", "http://salesmanbuddyserver.elasticbeanstalk.com/v1/salesmanbuddy/");
app.constant("baseUrl", "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/");
app.constant("clientId", "38235450166-qo0e12u92l86qa0h6o93hc2pau6lqkei.apps.googleusercontent.com");
app.constant("userInfoEndpoint", "users/me");
app.constant("usersPath", "users");
app.constant("dealershipsPath", "dealerships");
app.constant("statesPath", "states");
app.constant("licensesPath", "licenses");
app.constant("questionsPath", "questions");
app.constant("saveDataPath", "savedata");
app.constant("userExistsPath", "userExists");
app.constant("licenseImagePath", "licenseimage");
app.constant("userTreePath", "userTree");
app.constant("errorPath", "error");
app.constant("reportsPath", "reports");
app.constant("accessRights", {salesman:1, manager:2, sbUser:3});// page:required type level


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
	when('/newUser/:dealershipCode/:userType', { templateUrl: 'templates/newUser.html', controller: newUserCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/pricing', {templateUrl: 'templates/pricing.html', resolve: AuthServiceProvider.waitForLogin}).
	when('/how', {templateUrl: 'templates/how.html', resolve: AuthServiceProvider.waitForLogin}).
	when('/reportsManager', {templateUrl:'templates/reportsManager.html', controller: reportsManagerCtrl, resolve: AuthServiceProvider.waitForLogin }).
	otherwise({ redirectTo: '/comingSoon' });
}]);

app.config(['AuthServiceProvider', 'baseUrl', 'clientId', function(AuthServiceProvider, baseUrl, clientId){
		AuthServiceProvider.setRefreshUrl(baseUrl + 'refreshToken');
		AuthServiceProvider.setServerUrl(baseUrl + 'codeForToken');
		AuthServiceProvider.setClientID(clientId);
		AuthServiceProvider.pushScope('https://www.googleapis.com/auth/plus.me');
		AuthServiceProvider.pushScope('email');
		AuthServiceProvider.pushScope('profile');
		AuthServiceProvider.setRedirectURI('http://localhost:8080/salesmanBuddyAdmin');

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
		// updates the specified googleUserId or "" meaning themselves
		return genericFactory.request('post', baseUrl + usersPath + "/" + (googleUserId || ""), "error updateUserToDealershipCode", undefined, options);
	}

	factory.getGoogleUserObject = function(){
		// factory.userExists();
		// return genericFactory.request('get', baseUrl + usersPath + "/me", "error getGoogleUserObject", {cache:true});
		return genericFactory.request('get', 'https://www.googleapis.com/oauth2/v1/userinfo?alt=json', 'error getUserObject', undefined, {cache:true});
	}

	factory.getNameForUser = function(googleUserId){
		return genericFactory.request('get', baseUrl + usersPath + "/" + googleUserId + "/google/name", "errorgetNameForUser: " + googleUserId, undefined, {cache:true})
	}

	factory.getUsersForDealershipId = function(dealershipId){
		var options = {
			params:{
				dealershipId:dealershipId
			},
			cache:true
		};
		return genericFactory.request('get', baseUrl + usersPath, "error getUsersForDealershipId: " + dealershipId, undefined, options);
	}

	return factory;
});

app.factory('dealershipsFactory',function(baseUrl, dealershipsPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllDealerships = function(){
		return genericFactory.request('get', baseUrl + dealershipsPath, "error getAllDealerships");
	}

	factory.newDealership = function(dealership){
		return genericFactory.request('put', baseUrl + dealershipsPath, "error newDealership", dealership);
	}

	factory.updateDealership = function(dealership){
		return genericFactory.request('post', baseUrl + dealershipsPath, "error updateDealership", dealership);
	}

	factory.getDealershipForId = function(id){
		return genericFactory.request('get', baseUrl + dealershipsPath + "/" + id, "error getDealershipForId: " + id, undefined, {cache:true});
	}

	factory.getDealershipForCode = function(dealershipCode){
		var options = {
			params:{
				dealershipCode:dealershipCode
			}
		};
		return genericFactory.request('get', baseUrl + dealershipsPath, 'error getDealershipForCode: ' + dealershipCode, undefined, options);
	}

	return factory;
});

app.factory('errorFactory',function(baseUrl, errorPath, genericFactory, User, $location, $q){
	var factory = {};

	var sendError = function(message){
		return genericFactory.request("put", baseUrl + errorPath, 'error sendError' + message, message);
	}

	factory.sendErrorToSalesmanBuddy = function(message){
		var defer = $q.defer();
		message += "\n\nLocation Data: " + $location.path();
		User.initUser().then(function(data){
			sendError(message + "\n\nUser Data: " + angular.toJson(User.getUser())).then(function(data){
				defer.resolve(data);
			});
		}, function(data){
			sendError(message + "\n\nNo User Data").then(function(data){
				defer.resolve(data);
			});
		});
		return defer.promise;
	}

	return factory;
});

app.factory('reportsFactory',function(baseUrl, reportsPath, genericFactory){
	var factory = {};

	factory.requestReportNow = function(reportType, email, dealershipId){
		var options = {
			params:{
				reportType:reportType,
				email:email,
				dealershipId:dealershipId
			}
		};
		return genericFactory.request('put', baseUrl + reportsPath, "error sendReportNow", {}, options);
	}

	return factory;
});

app.factory('userTreeFactory',function(baseUrl, userTreePath, genericFactory, $q){
	var factory = {};

	factory.getAllUserTrees = function(){
		return genericFactory.request('get', baseUrl + userTreePath, "error getAllUserTrees");
	}

	factory.getUserTreesForGoogleUserId = function(googleUserId){
		var options = {
			params:{
				googleUserId:googleUserId
			}
		};
		return genericFactory.request('get', baseUrl + userTreePath, "error getUserTreesForGoogleUserId", undefined, options);
	}

	factory.getUserTreesForGoogleSupervisorId = function(googleSupervisorId){
		var options = {
			params:{
				googleSupervisorId:googleSupervisorId
			}
		};
		return genericFactory.request('get', baseUrl + userTreePath, "error getUserTreesForGoogleSupervisorId", undefined, options);
	}

	factory.getUserTreesForSBUserId = function(sbUserId){
		var options = {
			params:{
				sbUserId:sbUserId
			}
		};
		return genericFactory.request('get', baseUrl + userTreePath, "error getUserTreesForSBUserId", undefined, options);
	}

	factory.getUserTreesForDealershipId = function(dealershipId){
		var options = {
			params:{
				dealershipId:dealershipId
			}
		};
		return genericFactory.request('get', baseUrl + userTreePath, "error getUserTreesForDealershipId", undefined, options);
	}

	factory.deleteUserTreeById = function(id){
		var options = {
			params:{
				id:id
			}
		};
		return genericFactory.request('delete', baseUrl + userTreePath, 'error deleteUserTreeById: ' + id, undefined, options);
	}

	factory.newUserTree = function(userTree){
		return genericFactory.request('put', baseUrl + userTreePath, "error newUserTree", userTree);
	}

	factory.updateUserTree = function(userTree){
		return genericFactory.request('post', baseUrl + userTreePath, "error updateUserTree", userTree);
	}

	factory.saveUserTree = function(userTree){
		var defer = $q.defer();
		userTree.type = userTree.typeObject.value;
		userTree.userId = userTree.user.googleUserId;
		if(userTree.supervisor)
			userTree.supervisorId = userTree.supervisor.googleUserId;

		if(userTree.id)
			factory.updateUserTree(userTree).then(function(data){
				defer.resolve(data);
			});
		else
			factory.newUserTree(userTree).then(function(data){
				userTree.created = data.created;
				userTree.id = data.id;
				defer.resolve(data);
			});
		return defer.promise;
	}

	return factory;
});

app.factory('statesFactory',function(baseUrl, statesPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllStates = function(){
		return genericFactory.request('get', baseUrl + statesPath, "error getAllStates", undefined, {cache:true});
	}

	factory.getStateForId = function(id){
		return genericFactory.request('get', baseUrl + statesPath + '/' + id, "error getStateById: " + id, undefined, {cache:true})
	}

	return factory;
});

app.factory('licensesFactory',function(baseUrl, licensesPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllLicenses = function(){
		var options = {
			params:{
				all:true
			}
		};
		return genericFactory.request('get', baseUrl + licensesPath, "error getting all licenses", undefined, options);
	}

	factory.getAllLicensesForUser = function(googleUserId){
		var options = null;
		if(googleUserId){
			options = {
				params:{
					googleUserId:googleUserId
				}
			};
		}
		return genericFactory.request('get', baseUrl + licensesPath, "error getAllLicensesForUser", undefined, options);
	}

	factory.getAllLicensesForDealership = function(dealershipId){
		var options = null;
		if(dealershipId){
			options = {
				params:{
					dealershipId:dealershipId
				}
			};
		}
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

	factory.getFirstNameFromLicense = function(license){
		return factory.getAnswerTextForTag(license, 3);
	}

	factory.getLastNameFromLicense = function(license){
		return factory.getAnswerTextForTag(license, 4);
	}
	
	factory.getPhoneNumberFromLicense = function(license){
		return factory.getAnswerTextForTag(license, 5);
	}
	
	factory.getStockNumberFromLicense = function(license){
		return factory.getAnswerTextForTag(license, 2);
	}

	factory.getAnswerTextForTag = function(license, tag){
		for (var i = license.qaas.length - 1; i >= 0; i--) {
			if(license.qaas[i].question.tag == tag)
				return license.qaas[i].answer.answerText;
		};
		return "";
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
app.run(function ($rootScope, $http, User, AuthService, $location, usersFactory, $q, userInfoEndpoint, baseUrl, dealershipsFactory, accessRights) {
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
		User.initUser().then(function(){
			$rootScope.user = User.getUser();
			console.log(User.getUser())
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

	$rootScope.doesUserHaveAccessTo = function(what, exactly){
		if(User.getUser()){
			var type = User.getUser().sb.type;
			if(exactly)
				return (type == accessRights[what]);
			else
				return (type >= accessRights[what])
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




















