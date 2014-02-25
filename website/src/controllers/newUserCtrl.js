function newUserCtrl($scope, $routeParams, usersFactory){

	if($routeParams.dealershipCode.length < 10)
		console.log("invalid dealershipCode: " + $routeParams.dealershipCode);

	usersFactory.getGoogleUserObject().then(function(userInfo){
		console.log(userInfo);
	});
	
}