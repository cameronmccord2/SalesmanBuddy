var app = angular.module('SALESMANBUDDYADMIN', ['ngRoute']);

// app.value("baseUrl", "http://salesmanbuddytest1.elasticbeanstalk.com/v1/salesmanbuddy/");
app.value("baseUrl", "https://localhost/salesmanBuddy/v1/salesmanbuddy/");
app.value("usersPath", "users");
app.value("dealershipsPath", "dealerships");
app.value("statesPath", "states");
app.value("licensesPath", "licenses");
app.value("questionsPath", "questions");
app.value("saveDataPath", "savedata");
app.value("userExistsPath", "userExists");
app.value("licenseImagePath", "licenseimage");

app.config(['$routeProvider', function($routeProvider, $locationProvider) {
  $routeProvider.
	when('/comingSoon', { templateUrl: 'templates/comingSoon.html', controller: comingSoonCtrl }).
	when('/home', { templateUrl: 'templates/home.html', controller: homeCtrl }).
	when('/allUsers', { templateUrl: 'templates/allUsers.html', controller: allUsersCtrl }).
	when('/licensesList', { templateUrl: 'templates/licensesList.html', controller: licensesListCtrl }).
	when('/dealershipManager', { templateUrl: 'templates/dealershipManager.html', controller: dealershipManagerCtrl }).
    otherwise({ redirectTo: '/comingSoon' });
}]);


app.factory('genericFactory', function($http, $q){
	var factory = {};
	factory.request = function(verb, url, errorMessage, object, options){
		var defer = $q.defer();
		verb = verb.toLowerCase();
		console.log("make sure these are in the right categories")
		if(verb == 'get' || verb == 'delete'){
			if(url.length > 0){
				$http[verb](url, options).success(function(data){
					defer.resolve(data);
				}).error(function(data, status, headers, config){
					console.log(errorMessage || "", data, status, headers, config);
					defer.reject(data);
				});
				return defer.promise;
			}else
				console.log("you need to specify a valid url");
		}else if(verb == 'put' || verb == 'post' || verb == 'options'){
			
			if(url.length > 0){
				$http[verb](url, object, options).success(function(data){
					defer.resolve(data);
				}).error(function(data, status, headers, config){
					console.log(errorMessage || "", data, status, headers, config);
					defer.reject(data);
				});
				return defer.promise;
			}else
				console.log("you need to specify a valid url");
		}
		else
			console.log("you need to specify a valid restful verb");
	}
	return factory;
});

app.factory('usersFactory',function(baseUrl, usersPath, genericFactory){
	var factory = {};

	factory.userExists = function(){
		var user = {deviceType:2};

	}

	factory.getAllUsers = function(){
		return genericFactory.request('get', baseUrl + usersPath, "error getAllUsers");
	}

	factory.updateUserToType = function(googleUserId, type){
		var options = {
			params:{
				type:type
			}
		};
		return genericFactory.request('post', baseUrl + usersPath + "/" + googleUserId, "error updateUserToType", null, options);
	}

	factory.updateUserToDealershipCode = function(googleUserId, dealershipCode){
		var options = {
			params:{
				dealershipcode:dealershipCode
			}
		};
		return genericFactory.request('post', baseUrl + usersPath + "/" + googleUserId, "error updateUserToDealershipCode", undefined, options);
	}

	return factory;
});

app.factory('dealershipsFactory',function(baseUrl, dealershipsPath, genericFactory){
	var factory = {};

	factory.getAllDealerships = function(){
		return genericFactory.request('get', baseUrl + dealershipsPath, "error getAllDealerships");
	}

	factory.newDealership = function(dealership){
		return genericFactory.request('put', baseUrl, dealershipsPath, "error newDealership", dealership);
	}

	factory.updateDealership = function(dealership){
		return genericFactory.request('post', baseUrl, dealershipsPath, "error updateDealership", dealership);
	}

	return factory;
});

app.factory('statesFactory',function(baseUrl, statesPath, genericFactory){
	var factory = {};

	factory.getAllStates = function(){
		return genericFactory.request('get', baseUrl + statesPath, "error getAllStates");
	}

	return factory;
});

app.factory('licensesFactory',function(baseUrl, licensesPath, genericFactory){
	var factory = {};

	factory.getAllLicensesForUser = function(){
		return genericFactory.request('get', baseUrl + licensesPath, "error getAllLicensesForUser");
	}

	factory.getAllLicensesForDealership = function(){
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

app.factory('questionsFactory',function(baseUrl, questionsPath, genericFactory){
	var factory = {};

	factory.getAllQuestions = function(){
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

app.factory('licenseImageFactory',function(baseUrl, licenseImagePath, saveDataPath, genericFactory){
	var factory = {};

	factory.getLicenseImageForAnswerId = function(answerId){
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
app.run(function ($rootScope, $http) {
    $http.defaults.headers.common.authprovider = "google";
    $http.defaults.headers.common.Authorization = "Bearer ya29.1.AADtN_X_Qp24Kd0DqS-BVoXaX4XRnC07UFtP4H78rB4Uqd2X0iwTqWBNMN77JWNpLQ";
});




















