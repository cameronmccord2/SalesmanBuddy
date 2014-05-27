'use strict';

angular.module('salesmanBuddyApp')
.factory('licensesFactory', ['licensesPath', 'genericFactory', 'usersFactory', function (licensesPath, genericFactory, usersFactory) {

	var factory = {};

	factory.getAllLicenses = function(){
		var options = {
			params:{
				all:true
			}
		};
		return genericFactory.request('get', licensesPath, "error getting all licenses", undefined, options);
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
		return genericFactory.request('get', licensesPath, "error getAllLicensesForUser", undefined, options);
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
		return genericFactory.request('get', licensesPath, "error getAllLicensesForDealership", undefined, options);
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
	
}]);
