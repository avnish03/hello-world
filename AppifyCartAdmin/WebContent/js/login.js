app.config(function (localStorageServiceProvider) {
	localStorageServiceProvider
	.setStorageType('sessionStorage');
});
app.config(function($locationProvider) {
	$locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
		});
});

app.controller('LoginCtrl', function($scope, localStorageService, $cookies, $http, $window, $location) {
	$scope.hideAllAlerts = function() {
        $(".alert").hide();
	};
	$scope.doLogin = function() {
		if ($scope.username == null || $scope.password == null){
		    $scope.hideAllAlerts();
			$('.alert-values-required', $('.login-form')).show();
			return;
		};

		var url = "/AppifyCartAdmin/rest/admin/login.json" ;
		var postObject = new Object();
		postObject.username = $scope.username;
		postObject.password = $scope.password;

		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};

		$http.post(url, postObject, config).then(function(response){
			console.log( JSON.stringify("outside "+response.data, null, "    ") );

			if (response.data.status == 1){
			console.log( JSON.stringify("inside "+response.data, null, "    ") );
			    localStorageService.set("currentuser", $scope.username);
			    localStorageService.set("accesstoken", response.data.accesstoken)
			    localStorageService.set("signupmethod", response.data.signupmethod)
				if(response.data.apps.length > 0){
					localStorageService.set("apps", response.data.apps);
					$window.location.href = '/admin/MyApps.html';
				}else{
					$window.location.href = '/admin/DevelopmentAppCreator.html';   // before '/admin/AppifyMain.html';
				}
			}else{
                if(response.data.status == -1){
		            $scope.hideAllAlerts();
            	    $('.alert-user-not-exists', $('.login-form')).show();
                }else{
		            $scope.hideAllAlerts();
                    $('.alert-loginfailed', $('.login-form')).show();
                }
			}
		});
	};

	$scope.doRegister = function() {
		if(($scope.email != null) && ($scope.fullname != null)){
			var url = "/AppifyCartAdmin/rest/admin/register.json";
			var timezone = jstz.determine();
			var timezoneName = timezone.name();
			var signupmethod = "appifycartwebsite";
			var postObject = new Object();
			postObject.fullname = $scope.fullname;
			postObject.email = $scope.email;
			postObject.password = $scope.password;
			postObject.timezoneName = timezoneName;
		    postObject.signupmethod = signupmethod;

            localStorageService.set("currentEmail", $scope.email);
            localStorageService.set("signupmethod", signupmethod);
			var config = {
					headers: { 'Content-Type': 'application/json; charset=UTF-8'
					}
			};

			$http.post(url, postObject, config).then(function(response){
				if (response.data.status == 1){
				    localStorageService.set("signup_status", "new_user");
				    localStorageService.set("currentuser", $scope.email);
                    localStorageService.set("buildType", "development");
				    localStorageService.set("accesstoken", response.data.accesstoken)
					$window.location.href = '/admin/DevelopmentAppCreator.html';
				}else{
				    localStorageService.set("signup_status", "already_registered");
					$window.location.href = '/admin/login.html';
				}
			});
		}
	};

	$scope.showSendLoginPasswordMsg = function(){
		var currentEmail = localStorageService.get("currentEmail");
		if(currentEmail != null){
		var signup_status = localStorageService.get("signup_status");
		    if(signup_status == "already_registered"){
		        $scope.hideAllAlerts();
		        $('.alert-already-registerd', $('.login-form')).show();
		    }else{
		        $scope.hideAllAlerts();
    		    $('.alert-registerd', $('.login-form')).show();
		    }
			localStorageService.set("currentEmail", null);
		}
	};

	$scope.showSendLoginPasswordMsg();

	$scope.doSendResetPassword = function() {
		if($scope.passwordEmail == null)
		{	document.getElementById('forget-help-text').innerHTML = 'Please enter your registered email id';
			console.log('email is null in reset password');
			return false;
		} else {
		var url = "/AppifyCartAdmin/rest/admin/forgotPassword.json/" + $scope.passwordEmail;

		$http.get(url).then(function(response){
			//$window.location.href = '/admin/login.html';
			//Have check
			$scope.hideAllAlerts();
			$('.alert-passwordReset', $('.login-form')).show();
			$('.login-form').show();
            $('.forget-form').hide();
		});

		return false;
		}
	};

	$scope.doResetPassword = function() {
		if ($scope.rePassword == $scope.confirmRePassword){
			var url = "/AppifyCartAdmin/rest/admin/resetPassword.json";

			var postObject = new Object();
			postObject.resetId = $location.search().restId;
			postObject.password = $scope.rePassword;

			var config = {
					headers: { 'Content-Type': 'application/json; charset=UTF-8'
					}
			};

			$http.post(url, postObject, config).then(function(response){
				if (response.data.status == 1){
					$scope.username = response.data.email;
					$scope.password = $scope.rePassword;
					$scope.doLogin();
				}else{
					$scope.resetPasswordError = "Password reset failed";
				}
			});
		}else{
			$scope.resetPasswordError = "Password mismatched";
		}
	};

});
