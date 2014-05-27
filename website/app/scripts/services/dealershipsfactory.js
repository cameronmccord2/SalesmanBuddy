'use strict';

angular.module('salesmanBuddyApp')
.factory('dealershipsFactory', ['dealershipsPath', 'genericFactory', function (dealershipsPath, genericFactory) {

	var factory = {};

	factory.getAllDealerships = function(){
		return genericFactory.request('get', dealershipsPath, "error getAllDealerships");
	}

	factory.newDealership = function(dealership){
		return genericFactory.request('put', dealershipsPath, "error newDealership", dealership);
	}

	factory.updateDealership = function(dealership){
		return genericFactory.request('post', dealershipsPath, "error updateDealership", dealership);
	}

	factory.getDealershipForId = function(id){
		return genericFactory.request('get', dealershipsPath + "/" + id, "error getDealershipForId: " + id, undefined, {cache:true});
	}

	factory.getDealershipForCode = function(dealershipCode){
		var options = {
			params:{
				dealershipCode:dealershipCode
			}
		};
		return genericFactory.request('get', dealershipsPath, 'error getDealershipForCode: ' + dealershipCode, undefined, options);
	}

	return factory;

}]);
