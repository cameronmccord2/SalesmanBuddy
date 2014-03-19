function licensesListCtrl($scope, licensesFactory){

	$scope.loaded = false;

	licensesFactory.getAllLicensesForDealership().then(function(licenses){
		$scope.loaded = true;
		$scope.licenses = licenses;
	})
}