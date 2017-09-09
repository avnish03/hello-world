app.config(function (localStorageServiceProvider) {
	localStorageServiceProvider
	.setStorageType('sessionStorage');
});

app.controller('PricingCtrlRecurly', function($scope, localStorageService,  $cookies, $http, $window) {


	$scope.getUser = function() {
		return localStorageService.get("currentuser");
	}

    $scope.basicURLPlan = "https://appifycart.recurly.com/subscribe/appifycart-basic-588-1y-s/?email="+$scope.getUser() ;
    $scope.smallBusinessPlanURL = "https://appifycart.recurly.com/subscribe/appifycart-smallbusiness-1188-1y-s/??email="+$scope.getUser() ;
    $scope.professionalPlanURL = "https://appifycart.recurly.com/subscribe/appifycart-professional-3588-1y-s/?email="+$scope.getUser();

	$scope.getPayment = function() {
		var payments = localStorageService.get("payments");
		$scope.payment = null;

		if (payments != null){
			var numPayments = payments.length;
			if (numPayments > 0){
				$scope.payment = payments[0].productID;
			}	
		}
		
		$scope.paymentTitle = null
		
		if ($scope.payment == "1001"){
			$scope.paymentTitle = "AppifyCart Basic";
		}else if ($scope.payment == "1002"){
			$scope.paymentTitle = "AppifyCart Small Business";
		}else if ($scope.payment == "1003"){
			$scope.paymentTitle = "AppifyCart Enterprise";
		}
		
		return ($scope.payment == null);
	}
	
});
