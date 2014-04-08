function reportsManagerCtrl($scope, User, dealershipsFactory, userTreeFactory, usersFactory, $q, reportsFactory, rightsUpperSBUser, rightsSalesman, rightsManager, rightsSBUser){

	$scope.view = 'loading';
	$scope.dealerships = [];
	$scope.dealershipUsers = [];
	$scope.userTrees = [];
	$scope.selects = {};
	$scope.sendingOnDemandReport = false;

	$scope.types = {
		onTestDriveEmail:1,
		dailyTestDriveSummaryEmail:2,
		dailySalesmanTestDriveEmail:3,
		dailyDealershipSummaryEmail:4,
		weeklyTestDriveSummaryEmail:5,
		weeklySalesmanSummaryEmail:6,
		weeklyDealershipSummaryEmail:7,
		biMonthlyTestDriveSummaryEmail:8,
		biMonthlySalesmanSummaryEmail:9,
		biMonthlyDealershipSummaryEmail:10,
		monthlyTestDriveSummaryEmail:11,
		monthlySalesmanSummaryEmail:12,
		monthlyDealershipSummaryEmail:13
	};
	/*
	public final static Integer MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 13;
	.DAILY_SALESMAN_SUMMARY 14
	.WEEKLY_SALESMAN_SUMMARY 15
	.MONTHLY_SALESMAN_SUMMARY 16
	.DAILY_STOCK_NUMBERS 17
	.WEEKLY_STOCK_NUMBERS 18
	.MONTHLY_STOCK_NUMBERS 19
	*/

	$scope.reportAboutToOptions = [// about person to person
		{name:"On test drive", value:1},
		{name:"Daily Salesman Summary", value:3},// these need to be added to the server, everything above 13
		{name:"Weekly Salesman Summary", value:6},
		{name:"Monthly Salesman Summary", value:12}
	];

	$scope.summaryOptions = [// about category to person
		{name:"Daily Test Drive", value:2},
		{name:"Daily All Salesman", value:14},
		{name:"Daily Stock Numbers", value:17},
		{name:"Daily Dealership", value:4},
		{name:"Weekly Test Drive", value:5},
		{name:"Weekly All Salesman", value:15},
		{name:"Weekly Stock Numbers", value:18},
		{name:"Weekly Dealership", value:7},
		{name:"Monthly Test Drive", value:11},
		{name:"Monthly All Salesman", value:16},
		{name:"Monthly Stock Numbers", value:19},
		{name:"Monthly Dealership", value:13}
	];

	$scope.newReportTypes = [
		{name:"Dealership-Wide", value:1},
		{name:"Salesman-Specific", value:2}
	];

	$scope.blankFunction = function(){

	}

	$scope.sendAllReportsNow = function(){
		if(!($scope.selects && $scope.selects.chosenDealership && $scope.selects.chosenDealership.id && $scope.selects.nowEmail))
			return;
		$scope.sendingOnDemandReport = true;
		var promises = [];
		for (var i = $scope.summaryOptions.length - 1; i >= 0; i--) {
			promises.push(reportsFactory.requestReportNow($scope.summaryOptions[i].value, $scope.selects.nowEmail, $scope.selects.chosenDealership.id));
		};
		$q.all(promises).then(function(resolutions){
			$scope.sendingOnDemandReport = false;
			console.log(resolutions);
			for (var i = resolutions.length - 1; i >= 0; i--) {
				if(resolutions[i] != 'Email Sent')
					alert("error sending email: " + resolutions[i]);
			};
		});
	}

	$scope.sendReportNow = function(){
		if(!($scope.selects && $scope.selects.reportNowOption && $scope.selects.reportNowOption.value && $scope.selects.chosenDealership && $scope.selects.chosenDealership.id && $scope.selects.nowEmail))
			return;
		$scope.sendingOnDemandReport = true;
		reportsFactory.requestReportNow($scope.selects.reportNowOption.value, $scope.selects.nowEmail, $scope.selects.chosenDealership.id).then(function(data){
			console.log(data);
			$scope.sendingOnDemandReport = false;
		}, function(data){
			$scope.sendingOnDemandReport = false;
			alert("Sending that reports has failed. Salesman Buddy has been notified and is working on the problem. Thank you for your patience.");
		});
	}

	$scope.aboutToUserTrees = function(){
		var list = [];
		for (var i = 0; i < $scope.userTrees.length; i++) {
			if($scope.isAboutToUserTree($scope.userTrees[i]))
				list.push($scope.userTrees[i]);
		};
		return list;
	}

	$scope.summaryUserTrees = function(){
		var list = [];
		for (var i = 0; i < $scope.userTrees.length; i++) {
			if($scope.isSummaryUserTree($scope.userTrees[i]))
				list.push($scope.userTrees[i]);
		};
		return list;
	}

	$scope.checkForDuplicates = function(index, actualValue, userTree){
		for (var i = $scope.userTrees.length - 1; i >= 0; i--) {
			if(i == index)
				continue;

			if(!userTree)// allows us to provide one, else use the indexed one
				userTree = $scope.userTrees[index];

			if($scope.userTrees[i].user == userTree.user && $scope.userTrees[i].supervisor == userTree.supervisor && $scope.userTrees[i].typeObject == userTree.typeObject)
				return true;
		};
		return false;
	}

	$scope.isSummaryUserTree = function(userTree){
		return ($scope.summaryOptions.indexOf(userTree.typeObject) != -1);
	}

	$scope.isAboutToUserTree = function(userTree){
		return ($scope.reportAboutToOptions.indexOf(userTree.typeObject) != -1);
	}

	$scope.summaryColumns = [
		{name:"Report To", desiredWidthInPixels:100, type:"select", realColumnName:"user", options:$scope.dealershipUsers, ngChange:$scope.checkForDuplicates},
		{name:"Report About", desiredWidthInPixels:100, type:"select", realColumnName:"typeObject", options:$scope.summaryOptions, ngChange:$scope.checkForDuplicates}
	];

	$scope.aboutToColumns = [
		{name:"Salesman", desiredWidthInPixels:100, type:"select", realColumnName:"user", options:$scope.dealershipUsers, ngChange:$scope.checkForDuplicates},
		{name:"Supervisor", desiredWidthInPixels:100, type:"select", realColumnName:"supervisor", options:$scope.dealershipUsers, ngChange:$scope.checkForDuplicates},
		{name:"Report Type", desiredWidthInPixels:100, type:"select", realColumnName:"typeObject", options:$scope.reportAboutToOptions, ngChange:$scope.checkForDuplicates}
	];

	$scope.newAboutToReport = function(){
		if(!($scope.selects.aboutUser && $scope.selects.toUser && $scope.selects.reportOption)){// check to see if everything answered
			$scope.newDealershipErrorMessage = "You must answer all the options";
			return;
		}

		var newUserTree = {
			user:$scope.selects.aboutUser,
			userId:$scope.selects.aboutUser.googleUserId,
			supervisor:$scope.selects.toUser,
			supervisorId:$scope.selects.toUser.googleUserId,
			typeObject:$scope.selects.reportOption,
			type:$scope.selects.reportOption.value
		};

		if($scope.checkForDuplicates(null, null, newUserTree)){
			$scope.newDealershipErrorMessage = "This report already exists";
			return;
		}

		$scope.clearNewUserTree();

		userTreeFactory.saveUserTree(newUserTree).then(function(userTree){
			newUserTree.id = userTree.id;
			newUserTree.created = userTree.created;
			$scope.userTrees.push(newUserTree);
		});
	}

	$scope.newDealershipReport = function(){
		if(!($scope.selects.toUser && $scope.selects.reportOption)){// check to see if everything answered
			$scope.newDealershipErrorMessage = "You must answer all the options";
			return;
		}

		var newUserTree = {
			user:$scope.selects.toUser,
			userId:$scope.selects.toUser.googleUserId,
			supervisor:undefined,
			supervisorId:0,
			typeObject:$scope.selects.reportOption,
			type:$scope.selects.reportOption.value
		};

		if($scope.checkForDuplicates(null, null, newUserTree)){
			$scope.newDealershipErrorMessage = "This report already exists";
			return;
		}

		$scope.clearNewUserTree();

		userTreeFactory.saveUserTree(newUserTree).then(function(userTree){
			newUserTree.id = userTree.id;
			newUserTree.created = userTree.created;

			$scope.userTrees.push(newUserTree);
		});
	}

	$scope.deleteUserTree = function(userTree, index){
		$scope.userTrees.splice(index, 1);
		userTreeFactory.deleteUserTreeById(userTree.id).then(function(data){
			console.log(data);
		});
	}

	$scope.clearNewUserTree = function(){
		$scope.newDealershipErrorMessage = "";
		$scope.selects.aboutUser = null;
		$scope.selects.toUser = null;
		$scope.selects.reportOption = null;
	}


	$scope.getAdjustedColumnWidth = function(width, columns, rowWidth){
		var count = 0;
		for (var i = columns.length - 1; i >= 0; i--) {
			count += columns[i].desiredWidthInPixels;
		};
		return {width:"" + (width * rowWidth / count) + "px"};
	}

	$scope.setupUserTrees = function(){
		// match dealership users up with the userTree
		for (var i = $scope.userTrees.length - 1; i >= 0; i--) {
			for (var j = $scope.dealershipUsers.length - 1; j >= 0; j--) {
				if($scope.userTrees[i].userId == $scope.dealershipUsers[j].googleUserId)
					$scope.userTrees[i].user = $scope.dealershipUsers[j];
				
				if($scope.userTrees[i].supervisorId == $scope.dealershipUsers[j].googleUserId)
					$scope.userTrees[i].supervisor = $scope.dealershipUsers[j];
			};
			// get type from summaryOptions
			for (var j = $scope.summaryOptions.length - 1; j >= 0; j--) {
				if($scope.userTrees[i].type == $scope.summaryOptions[j].value){
					$scope.userTrees[i].typeObject = $scope.summaryOptions[j];
					break;
				}
			};
			// get type from reportAboutToOptions
			for (var j = $scope.reportAboutToOptions.length - 1; j >= 0; j--) {
				if($scope.userTrees[i].type == $scope.reportAboutToOptions[j].value){
					$scope.userTrees[i].typeObject = $scope.reportAboutToOptions[j];
					break;
				}
			};
		};
	}

	User.initUser().then(function(){
		if($scope.doesUserHaveAccessTo([rightsSBUser])){
			dealershipsFactory.getAllDealerships().then(function(dealerships){
				$scope.dealerships = dealerships;
				$scope.view = 'main';
			});
		}else if($scope.doesUserHaveAccessTo([rightsManager], true)){
			$scope.selects.chosenDealership = {
				id:User.getUser().sb.dealershipId
			};
			console.log(User.getUser().sb.dealershipId)
			userTreeFactory.getUserTreesForDealershipId(User.getUser().sb.dealershipId).then(function(userTrees){
				usersFactory.getUsersForDealershipId(User.getUser().sb.dealershipId).then(function(users){
					$scope.userTrees = userTrees;
					$scope.setupDealershipUsers(users);
					$scope.setupUserTrees();
					$scope.view = 'main';
				});
			});
		}else
			$scope.view = 'noRights';
	});

	$scope.selectedDealership = function(){
		$scope.view = 'loadingLower';

		userTreeFactory.getUserTreesForDealershipId($scope.selects.chosenDealership.id).then(function(userTrees){
			usersFactory.getUsersForDealershipId($scope.selects.chosenDealership.id).then(function(users){
				$scope.userTrees = userTrees;
				$scope.setupDealershipUsers(users);
				$scope.setupUserTrees();
				$scope.view = 'main';
			});
		});
	}

	$scope.getNameForUser = function(user){
		usersFactory.getNameForUser(user.googleUserId).then(function(userData){
			user.name = userData.name;
		});
	}

	$scope.setupDealershipUsers = function(users){
		$scope.dealershipUsers.length = 0;
		for (var i = 0; i < users.length; i++) {
			$scope.dealershipUsers.push(users[i]);
			$scope.getNameForUser($scope.dealershipUsers[i]);
		};
	}
}



































