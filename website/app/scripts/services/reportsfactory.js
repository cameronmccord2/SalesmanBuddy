'use strict';

angular.module('salesmanBuddyApp')
.factory('reportsFactory', ['reportsPath', 'genericFactory', function (reportsPath, genericFactory) {

	var factory = {};

	factory.requestReportNow = function(reportType, email, dealershipId){
		var options = {
			params:{
				reportType:reportType,
				email:email,
				dealershipId:dealershipId
			}
		};
		return genericFactory.request('put', reportsPath, "error sendReportNow", {}, options);
	}

	return factory;

}]);
