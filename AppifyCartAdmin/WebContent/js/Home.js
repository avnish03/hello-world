var app = angular.module('MyApp', ['LocalStorageModule', 'ngCookies', 'ngRoute'])
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

app.factory('httpInterceptor', function ($window, localStorageService) {
  return {
    request: function (config) {
      if(localStorageService != null){
            config.headers['Authorization'] = 'Bearer ' + localStorageService.get("accesstoken");
            config.headers['Accept'] = 'application/json;odata=verbose';
      }

      return config;
    },

    responseError : function(responseError){
        $window.location.href = '/admin/login.html';
    }
}
});

app.config(function ($httpProvider) {
  $httpProvider.interceptors.push('httpInterceptor');
});

app.controller('HomeCtrl', function($scope, localStorageService, $cookies, $http, $window, $location) {

    $scope.resetPasswordDiv = false ;
    $scope.changePasswordButton = true;
    $scope.isShopifyAppUser = false ;
    var queryParams = $location.search();
    var user = queryParams["user"];
    var accesstoken = queryParams["accesstoken"];
    if(user != null){
        localStorageService.set("currentuser", user);
        localStorageService.set("accesstoken", accesstoken);
       }

	$scope.loadApps = function() {
		var user = localStorageService.get("currentuser");
		var url = "/AppifyCartAdmin/rest/admin/appData.json/" + user;
        if(user != null){
            $http.get(url).then(function(response){
                if (response.data.status == 1){
                    if(response.data.apps.length > 0){
                        localStorageService.set("apps", response.data.apps);
                        localStorageService.set("shopifyshop", response.data.apps[0].website);
                        localStorageService.set("payments", response.data.payments);
                    }
                }
            });
		}
	}

	$scope.loadPage = function(){
		var user = localStorageService.get("currentuser");
		if(user == null){
			$window.location.href = '/admin/login.html';
		}else{
		    var signupmethod = localStorageService.get("signupmethod");
		    if(signupmethod == "shopifyapp"){
		        var currentPath = $location.path();
		        $scope.isShopifyAppUser = true;
                var isPricingPage = false;
                if(currentPath.indexOf("AppifyPricing")!== -1 ){
                    isPricingPage = true
                }
		        var freeTrialStatus = localStorageService.get("freeTrialStatus");
		        if((freeTrialStatus == "NA") && (!isPricingPage) ){
			        $window.location.href = '/admin/AppifyPricing.html';
		        }
		    }

		}
	}
	$scope.openAppifyMain = function(){
		localStorageService.set("currentos", "ios");
    	localStorageService.set("buildType", "development");
    	$window.location.href = '/admin/DevelopmentAppCreator.html';  // before it was '/admin/AppifyMain.html';
    }
    $scope.openMyApps = function(){
   		$window.location.href = '/admin/MyApps.html';
   	}

    $scope.setSignupMode = function(){
    	var user = localStorageService.get("currentuser");
    	if(user != null){
            var url = "/AppifyCartAdmin/rest/admin/isSiteAccessible.json/" + user;
            $http.get(url).then(function(response){
                if(response.data.status == 1){
                    var signupmethod = response.data.signupmethod;
                        localStorageService.set("signupmethod", signupmethod);
                    }
            });
		}
   	}

	$scope.loadPage();

	$scope.loadApps();

	// $scope.setSignupMode(); // commented for now

	$scope.createEditIOSDevelopment = function() {
		localStorageService.set("currentos", "ios");
		localStorageService.set("buildType", "development");
		$window.location.href = '/admin/DevelopmentAppCreator.html';
	};
	$scope.createEditIOS = function() {
		localStorageService.set("currentos", "ios");
		localStorageService.set("buildType", "appStore");
		$window.location.href = '/admin/AppCreator.html';
	};
	$scope.createEditAndroidDevelopment = function() {
		localStorageService.set("currentos", "android");
		localStorageService.set("buildType", "development");
        $window.location.href = '/admin/DevelopmentAppCreator.html';
	};
	$scope.createEditAndroid = function() {
		localStorageService.set("currentos", "android");
		localStorageService.set("buildType", "appStore");
		$window.location.href = '/admin/AppCreator.html';
	};


	$scope.logout = function(){
		localStorageService.clearAll();
		$cookies.remove("currentuser");
		$window.location.href = '/admin/login.html';
	};

/*	  from here , codes added by avnish       */

	$scope.fetchProfile= function() {
	    var user = localStorageService.get("currentuser");
        var customerDetailsURL = "/AppifyCartAdmin/rest/admin/profile.json/"+user;
        $http.get(customerDetailsURL).then(function(response){
        	if(response.data != null){
            //    $scope.customer_name = response.data.organisation;
                $scope.customer_email = response.data.userDetails.accountemail;
                $scope.organisation = response.data.userDetails.organisation;
                $scope.created_at = response.data.userDetails.created_at;
                $scope.updated_at = response.data.userDetails.updated_at;
            }
        });
	}

	$scope.fetchApps= function() {
    	    var user = localStorageService.get("currentuser");
            var customerDetailsURL = "/AppifyCartAdmin/rest/admin/profile.json/"+user;
            $http.get(customerDetailsURL).then(function(response){
            	if(response.data != null){
                //    $scope.customer_name = response.data.organisation;
                    $scope.customer_email = response.data.userDetails.accountemail;
                    $scope.organisation = response.data.userDetails.organisation;
                    $scope.created_at = response.data.userDetails.created_at;
                    $scope.updated_at = response.data.userDetails.updated_at;
                }
            });
    	}

	$scope.myProfile = function(){
        $window.location.href = '/admin/MyProfile.html';
	};
	$scope.myApps = function(){
        $window.location.href = '/admin/MyApps.html';
	};

    $scope.hide = function() {
            $scope.changePasswordButton="false";
             $scope.savePasswordButton="true";
    }

	$scope.resetPassword = function() {
	    var user = localStorageService.get("currentuser");

		if (user == null || $scope.old_password == null){
		           $scope.resetPasswordMessage = "Error : Empty old password";
			return;
		}

		var url = "/AppifyCartAdmin/rest/admin/login.json" ;
		var postObject = new Object();
		postObject.username = user;
		postObject.password = $scope.old_password;


		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};

		$http.post(url, postObject, config).then(function(response){
			if (response.data.status == 1){
	           $scope.resetPasswordMessage = "Processing";
		        $scope.doResetPassword();   // call the resetting password method
			}else{
		           $scope.resetPasswordMessage = "Old Password Mismatch";
			}
		});
	}

	$scope.doResetPassword = function() {
		    var user = localStorageService.get("currentuser");
	 			if ($scope.new_password == $scope.confirm_password){
        			var url = "/AppifyCartAdmin/rest/admin/resetSavePassword.json/"+user+"/"+$scope.new_password;

        			var postObject = new Object();
        			postObject.password = $scope.confirm_password;

        			var config = {
        					headers: { 'Content-Type': 'application/json; charset=UTF-8'
        					}
        			};

        			$http.post(url, postObject, config).then(function(response){
        				if (response.data.status == 1){
        					$scope.resetPasswordMessage = "Password reset success";
        					$scope.cancelPasswordButton=true;
        				}else{
        					$scope.resetPasswordMessage = "Password reset failed";
        				}
        			});
        		}else{
        			$scope.resetPasswordMessage = "New Password and Repeat Password mismatched";
        		}
	}


     $scope.changePasswordDivToggle = function() {
         $scope.resetPasswordDiv = true ;
         $scope.changePasswordButton = false;
     }

     $scope.cancelResetPassword = function() {
              $scope.resetPasswordDiv = false ;
              $scope.changePasswordButton = true;
     }
        /* my account dropdown action
           clicks on the button, toggle between hiding and showing the dropdown content
        */
    $scope.myAccount = function() {
        document.getElementById("myDropdown").classList.toggle("show");
    }

    $scope.tempFlag = false;
    $scope.toggleClassDropdownMenu = function($event){
        var classToAppend = "open";
        $scope.tempFlag = ($scope.tempFlag==true)?false:true;
        if($scope.tempFlag)
            angular.element($event.target).parent().addClass('open');
        else
             angular.element($event.target).parent().removeClass('open');

    }
    // Close the dropdown if the user clicks outside of it
    window.onclick = function(event) {
      if (!event.target.matches('.dropbtn')) {

        var dropdowns = document.getElementsByClassName("dropdown-content");
        var i;
        for (i = 0; i < dropdowns.length; i++) {
          var openDropdown = dropdowns[i];
          if (openDropdown.classList.contains('show')) {
            openDropdown.classList.remove('show');
          }
        }
      }
    }

    $scope.fetchDashboardData= function() {
    $scope.active_users_count = '';
	    var user = localStorageService.get("currentuser");
        var dashboardURL = "/AppifyCartAdmin/rest/admin/dashboard.json/"+user;
        $http.get(dashboardURL).then(function(response){
        	if(response.data != null){
        	console.log( 'fetching dashboard data' );
        	//console.log( JSON.stringify(response.data, null, "    ") );
        	console.log( 'active : '+ response.data.active_users );
                $scope.active_users_count = '10' ;
        	console.log( 'active 2 : '+ $scope.active_users_count  );

            }
        });
	}



/*	  codes added by avnish ends here     */

});
