'use strict';

angular.module('salesmanBuddyApp')
.factory('stockNumbersFactory', ['stockNumbersPath', 'genericFactory', function (stockNumbersPath, genericFactory) {

	var factory = {};

	factory.getAllStockNumbers = function(){
		var options = {
			params:{
				all:true
			}
		};
		return genericFactory.request('get', stockNumbersPath, 'error getAllStockNumbers', undefined, options);
	}

	factory.getStockNumbersForDealershipId = function(dealershipId){
		var options = {
			params:{
				dealershipId:dealershipId || 0
			}
		};
		return genericFactory.request('get', stockNumbersPath, 'error getStockNumbersForDealershipId: ' + dealershipId, undefined, options);
	}

	factory.getStockNumbers = function(){
		return genericFactory.request('get', stockNumbersPath, 'error getStockNumbers for me');
	}

	factory.getStockNumberById = function(id){
		return genericFactory.request('get', stockNumbersPath + '/' + id, 'error getStockNumberById: ' + id);
	}

	factory.newStockNumber = function(stockNumber, status){
		var options = {
			params:{
				stockNumber:stockNumber,
				status:status
			}
		};
		return genericFactory.request('put', stockNumbersPath, 'error putNewStockNumber: ' + angular.toJson(stockNumber), {}, options);
	}

	factory.updateStockNumber = function(stockNumber){
		return genericFactory.request('post', stockNumbersPath, 'error updateStockNumber: ' + angular.toJson(stockNumber), stockNumber);
	}

	factory.deleteStockNumberById = function(id){
		return genericFactory.request('delete', stockNumbersPath, 'error deleteStockNumberById: ' + id);
	}

	return factory;
	
}]);
