function licensesListCtrl($scope, licensesFactory, User, dealershipsFactory, usersFactory){

	$scope.view = "loading";
	$scope.licenses = [];
	$scope.dealershipUsers = [];
	$scope.allUsers = [];
	$scope.dealerships = [];
	$scope.selected = {};
	
	User.initUser().then(function(){
		if(User.getUser().sb.type == 1)// get them for this user
			$scope.selectedUser(User.getUser().sb.googleUserId);
		else if(User.getUser().sb.type == 2)// choose whole dealership or specific user
			$scope.selectedDealership(User.getUser().sb.dealershipId);
		else if(User.getUser().sb.type > 2){// choose all licenses, specific dealership, or specific user
			$scope.getAllDealerships();
			$scope.getAllUsers();
		}else
			console.log("invalid user type found: ", User.getUser());
	});

	$scope.getAllLicensesForDealership = function(dealershipId){
		licensesFactory.getAllLicensesForDealership(dealershipId).then(function(licenses){
			$scope.licenses = licenses;
			$scope.view = "main";
		});
	}

	$scope.selectedDealership = function(dealership){
		console.log(dealership)
		usersFactory.getUsersForDealershipId(dealership.id).then(function(users){
			$scope.dealershipUsers = users;
			for (var i = $scope.dealershipUsers.length - 1; i >= 0; i--) {
				$scope.getNameForUser($scope.dealershipUsers[i]);
			};
			$scope.view = "main";
		});
	}

	$scope.selectedUser = function(user){
		licensesFactory.getAllLicensesForUser(user.googleUserId).then(function(licenses){
			$scope.licenses = licenses;
			$scope.view = "main";
		});
	}

	$scope.getAllUsers = function(){
		usersFactory.getAllUsers().then(function(users){
			$scope.allUsers = users;
			for (var i = $scope.allUsers.length - 1; i >= 0; i--) {
				$scope.getNameForUser($scope.allUsers[i]);
			};
			$scope.view = "main";
		});
	}

	$scope.getAllDealerships = function(){
		dealershipsFactory.getAllDealerships().then(function(dealerships){
			console.log(dealerships)
			$scope.dealerships = dealerships;
			$scope.view = "main";
		});
	}

	$scope.getNameForUser = function(user){
		usersFactory.getNameForUser(user.googleUserId).then(function(userData){
			console.log(userData);
			user.name = userData.name;
		});
	}
}