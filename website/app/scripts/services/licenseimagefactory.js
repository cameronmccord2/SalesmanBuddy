'use strict';

angular.module('salesmanBuddyApp')
.factory('licenseImageFactory', ['licenseImagePath', 'saveDataPath', 'genericFactory', 'usersFactory', function (licenseImagePath, saveDataPath, genericFactory, usersFactory) {

	var factory = {};

	factory.getLicenseImageForAnswerId = function(answerId){
		// usersFactory.userExists();
		var options = {
			params:{
				answerid:answerId
			}
		};
		return genericFactory.request('get', licenseImagePath, "error getLicenseImageForAnswerId");
	}

	factory.saveLicenseImage = function(){
		// not implemented in admin
	}

	return factory;
	
}]);
