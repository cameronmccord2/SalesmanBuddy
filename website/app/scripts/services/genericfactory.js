'use strict';

angular.module('salesmanBuddyApp')
.factory('genericFactory', ['$http', '$q', 'baseUrlLocal', 'baseUrlAWS', function ($http, $q, baseUrlLocal, baseUrlAWS) {
	
	var factory = {};
	factory.request = function(verb, url, errorMessage, object, options, dontAddBaseUrl){
		var defer = $q.defer();
		verb = verb.toLowerCase();
		if(!dontAddBaseUrl)
			url = factory.getBaseUrl() + url;

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

	factory.getBaseUrl = function(){
		if(window.location.host == 'localhost:8080' || window.location.host == 'localhost:9000')
			return baseUrlLocal;
		else if(window.location.host == 'salesmanbuddy.com')
			return baseUrlAWS;

		throw new Error("App is running in an unknown environment. Please check URL in browser");
	}
	return factory;

}]);
