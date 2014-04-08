function newUserCtrl($scope, $routeParams, $location, usersFactory, User, dealershipsFactory, errorFactory, statesFactory){

	$scope.errorMessage = "";
	$scope.view = "loading";
	$scope.newUserType = $routeParams.userType;
	$scope.dealershipCode = $routeParams.dealershipCode;

	if($routeParams.dealershipCode.length < 10){
		console.log("invalid dealershipCode: " + $routeParams.dealershipCode);
		$scope.errorMessage = "You have a bad dealership code";
		$scope.view = "error";
	}

	dealershipsFactory.getDealershipForCode($routeParams.dealershipCode).then(function(dealership){
		$scope.dealership = dealership;
		$scope.view = "main";
		statesFactory.getStateForId($scope.dealership.stateId).then(function(state){
			$scope.dealership.state = state.name;
		});
	});

	$scope.confirmDealership = function(){
		$scope.view = "loading";
		$scope.loadingMessage = "Please wait while we update your user account.";
		User.initUser().then(function(){
			var type = 0;
			if($routeParams.userType == 'salesman')
				type = 1;
			else if($routeParams.userType == 'manager')
				type = 3;
			usersFactory.updateUserToDealershipCode(User.getUser().sb.googleUserId, $routeParams.dealershipCode, type).then(function(data){
				$scope.view = "finished";
			});
		});
	}
	
	$scope.denyDealership = function(){
		var userMessage = prompt("We are sorry about this inconvenience, an error report will be sent to Salesman Buddy support. If you want to include your contact info we can contact you directly to resolve the problem if needs be.", "");
		errorFactory.sendErrorToSalesmanBuddy("They denied the dealership, id: " + $scope.dealership.id + ", code: " + $routeParams.dealershipCode + "\n\nUser Message: " + (userMessage || "No message")).then(function(data){
			$location.path("/home");
		});
	}
}