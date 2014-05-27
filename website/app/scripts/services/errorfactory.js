'use strict';

angular.module('salesmanBuddyApp')
.factory('errorFactory', ['errorPath', 'genericFactory', 'User', '$location', '$q', function (errorPath, genericFactory, User, $location, $q) {

	var factory = {};

	var sendError = function(message){
		return genericFactory.request("put", errorPath, 'error sendError' + message, message);
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

}]);
