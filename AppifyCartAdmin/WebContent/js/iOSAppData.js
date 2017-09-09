app.controller('iOSAppDataCtrl', function($scope, fileUpload, localStorageService, $cookies, $http, $location, $anchorScroll, $window) {
	$scope.buildType = "development";
	$scope.developmentBuild = false;
	$scope.modal_box = false;
	$scope.modal_box_Production = false ;
	$scope.isShopifyAppUser = false ;
    $scope.store_shopify_accessToken = "NA";

    $scope.loadOSCSS = function(ostype){
            angular.element(document.querySelector('#mobile-icon-'+ostype)).addClass("mobile-iconActive");
            angular.element(document.querySelector('#mobile-icon-'+ostype)).children().addClass("phone-iconActive");
            angular.element(document.querySelector('#mobile-icon-'+ostype)).children().removeClass("phone-icon");
    }
	$scope.setBuildType = function(){
	    var currentPath = $location.path();
        if(currentPath.indexOf("DevelopmentAppCreator")!== -1 ){
            localStorageService.set("buildType", "development");
        } else {
            localStorageService.set("buildType", "appStore");
        }
	}
	$scope.getShopifyAccessToken= function(){
//		var user =  localStorageService.get("currentuser");
//	    var getShopifyAccessTokenUrl = "/AppifyCartAdmin/rest/admin/shopifyAccessToken.json/"+user;
//            $http.get(getShopifyAccessTokenUrl).then(function(response){
////                console.log( JSON.stringify(response.data, null, "    ") );
////                $scope.store_shopify_accessToken = response.data.shopifyAccessToken;
//            });
        if($scope.isShopifyAppUser)
            $scope.store_shopify_accessToken = "accessTokenNeeded"; // getting access token from db
        else
            $scope.store_shopify_accessToken = "NA"; // getting access token from db

	}
	$scope.loadAppData = function(){
		$scope.platform = null;
		$scope.name_of_app = null;
		$scope.store_website = null;
		$scope.store_api_key = null;
		$scope.store_api_password = null;

        // set the build type by looking  into url of browser for creating build
        $scope.setBuildType();

		var apps = localStorageService.get("apps");
		var signupmethod = localStorageService.get("signupmethod");

		if (apps != null){
			var numApps = apps.length;
			//Default setting, even loop is not there
			$scope.ostype = localStorageService.get("currentos");
			for (var i = 0; i < numApps; i++) {
				var app = apps[i];
				if (app.ostype == localStorageService.get("currentos")){
					$scope.ostype = app.ostype;
					$scope.platform = app.platform;
					$scope.name_of_app = app.appname;
					$scope.store_website = app.website;
					$scope.store_api_key = app.apikey;
					$scope.store_api_password = app.apipassword;
                    //  $scope.store_shopify_accessToken = app.shopifyAccessToken; // getting access token from db
					break;
				}
			}
		}
		if ($scope.ostype == null){
			$scope.ostype = "ios";
		}
		if (signupmethod === "shopifyapp"){
			$scope.isShopifyAppUser = true;
			$scope.store_api_key = "shopify_app_user";
			$scope.store_api_password = "shopify_app_user";
            $scope.store_website = localStorageService.get("shopifyshop");
		}

		$scope.androidEnabled = ($scope.ostype != "ios");
		$scope.buildType = localStorageService.get("buildType");
		$scope.loadOSCSS($scope.ostype);
		// get shopify accesstoken
        $scope.getShopifyAccessToken();
	}


	$scope.loadAppData();

	$scope.osChanged = function(osType){
		localStorageService.set("currentos", osType);
		$scope.loadAppData();
	}
	$scope.hideAllAlerts = function() {
        $(".alert").hide();
	};
	$scope.scrollToAlerts = function(){
        $location.hash('page-content');
        $anchorScroll();
	}
	$scope.returnCreateDevelopmentBuild = function(){
	console.log("create develellellelele");
        var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
        var foo = document.getElementById("buildAlert");
        foo.click();
        if(isDevelopment){
            $scope.doCreateDevelopmentApp();
            return true;
        }
	}
	$scope.validateApiPair=function(){
	console.log("$scope.isShopifyAppUser : "+$scope.isShopifyAppUser);

	    if($scope.isShopifyAppUser){
            console.log("create develellellelele");
                    var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
                    var foo = document.getElementById("buildAlert");
                    if(isDevelopment){
                        foo.click();
                        $scope.doCreateDevelopmentApp();
                        return true;
                    }
	    } else {
            if($scope.isEmpty($scope.store_website,"store_websiteID") ||
                $scope.isEmpty($scope.store_api_key,"store_api_keyID") ||
                    $scope.isEmpty($scope.store_api_password,"store_api_passwordID"))
                        return false;
            var url = "/AppifyCartAdmin/rest/admin/validateApi.json?storeUrl="+ $scope.store_website
                                +"&apiKey="+ $scope.store_api_key
                                    +"&apiPassword="+$scope.store_api_password;

            $http.get(url).then(function(response){
                if(response.data.status == "failed"){
                    $('.alert-api-validation-failed', $('.form-horizontal')).show();
                    $scope.scrollToAlerts();
                    return false;
                }else{
                   console.log("create development");
                           var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
                           var foo = document.getElementById("buildAlert");
                           foo.click();
                           if(isDevelopment){
                               $scope.doCreateDevelopmentApp();
                               return true;
                           }
                }
            });
        }
	}

	$scope.doCreateApp= function() {

        $scope.validateApiPair();
		console.log("scope.doCreateApp() called");
		var user =  localStorageService.get("currentuser");
		var ostype = $scope.ostype;
		var fileUrl = "/AppifyCartAdmin/rest/admin/fileupload.json" + "/" + user + "/" + ostype + "/" + "build" + "/" + 0;
		$scope.uploadFiles(fileUrl);
	};

	$scope.doCreateDevelopmentApp = function() {
		console.log("scope.doCreateDevelopmentApp() called");
		var user =  localStorageService.get("currentuser");
		var ostype = $scope.ostype;

/**
*       added by ankur for buildtype
*/
		if (ostype == "ios"){
        	    localStorageService.set("currentos", "ios");
        		localStorageService.set("buildType", "development");
        		}else if(ostype == "android"){
        		        localStorageService.set("currentos", "android");
                		localStorageService.set("buildType", "development");
        		}

		var fileUrl = "/AppifyCartAdmin/rest/admin/fileupload.json" + "/" + user + "/" + ostype + "/" + "build" + "/" + 0;
		$scope.uploadFiles(fileUrl);
	};

	$scope.isEmpty=function(temp,id){
		if(temp == null || temp == "")
		    return true ;
        else
			return false;
	};

    $scope.doValidateApp=function(){
         $scope.hideAllAlerts();
         if($scope.isEmpty($scope.platform,"store_plateform_list"))
            $scope.platform = "Shopify";
        if($scope.isEmpty($scope.name_of_app,"name_of_appID") || $scope.isEmpty($scope.store_website,"store_websiteID") ||
            $scope.isEmpty($scope.store_api_key,"store_api_keyID") || $scope.isEmpty($scope.store_api_password,"store_api_passwordID") || $scope.isEmpty($scope.app_language,"app_language") ){
            $('.alert-errors-in-form', $('.form-horizontal')).show();
            $scope.scrollToAlerts();
    	    return false;
		}
	    else{
	        $scope.validateApiPair();
	    }

    };

	$scope.uploadFiles = function(fileUrl){
		$scope.uploadAppIcon(fileUrl);
	};

/* js ( commented ) for building app for both iOS and Android on single click is here

	$scope.uploadAppIcon = function(fileUrl){
	       var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
           console.log('Development build  is '+isDevelopment);
            if(isDevelopment){
                //$scope.developmentIOSBuild(fileUrl);
                $scope.developmentAndroidApp(fileUrl);
            }
            else{
                 if ($scope.ostype == "ios"){
                    fileUpload.uploadFileToUrl($scope.appIconFile, $scope.uploadSplashScreenFile, fileUrl);
    			 }
                else{
                    fileUpload.uploadFileToUrl($scope.appIconFile, $scope.uploadSplashScreenFile, fileUrl);
                    fileUpload.uploadFileToUrl($scope.appIconFile, $scope.uploadSplashScreenFile, fileUrl);
    			}
    		}
	};

*/


	$scope.uploadAppIcon = function(fileUrl){
        if ($scope.ostype == "ios"){
            var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
            console.log(localStorageService.get("buildType"));
            console.log('Development IOS build  is '+isDevelopment);
            if(isDevelopment){
                $scope.developmentIOSBuild(fileUrl);
            } else {
                fileUpload.uploadFileToUrl($scope.appIconFile, $scope.uploadSplashScreenFile, fileUrl);
            }
        } else {
            var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
            console.log('Development Android build  is '+isDevelopment);
            if(isDevelopment){
                $scope.developmentAndroidApp(fileUrl);
            } else {
                fileUpload.uploadFileToUrl($scope.appIconFile, $scope.uploadSplashScreenFile, fileUrl);
            }
        }
		// fileUpload.uploadFileToUrl($scope.appIconFile, $scope.uploadSplashScreenFile, fileUrl);
	};

	$scope.uploadSplashScreenFile = function(fileUrl, iconFileName){
		$scope.iconFileName = iconFileName;
		if ($scope.ostype == "ios"){
    	    fileUpload.uploadFileToUrl($scope.splashScreenFile, $scope.uploadIosCertificate,  fileUrl);
		}
		else{
  		    fileUpload.uploadFileToUrl($scope.splashScreenFile, $scope.uploadAndroidKeyStore,  fileUrl);
		}
	};

	$scope.uploadIosCertificate = function(fileUrl, splashFileName){
		$scope.splashFileName = splashFileName;
		fileUpload.uploadFileToUrl($scope.iosCertificate, $scope.uploadIosProvProfile, fileUrl);
	};

	$scope.uploadIosProvProfile = function(fileUrl, certificateFileName){
		$scope.certificateFileName = certificateFileName;
		fileUpload.uploadFileToUrl($scope.iosProvProfile, $scope.createApp, fileUrl);
	};

    $scope.developmentIOSBuild = function(fileUrl){
        console.log('developmentIOSBuild');
        $scope.iconFileName = "default";
        $scope.splashFileName = "default";
        $scope.certificateFileName = "default";
        $scope.passphrase = "default";
        $scope.bundleId = "default";
        var provisioningProfileFileName = "default";
        $scope.createApp(fileUrl, provisioningProfileFileName);
    }

	$scope.createApp = function(fileUrl, provisioningProfileFileName){

		var url = "/AppifyCartAdmin/rest/admin/iOSAppData.json";
        console.log('createApp ios started');
        $scope.modal_box = true;
        $scope.modal_box_Production = true ;

		var postObject = new Object();
		postObject.accountEmail = localStorageService.get("currentuser");
		postObject.osType = "ios";
		postObject.platform = $scope.platform;
		postObject.appName = $scope.name_of_app;
		postObject.website = $scope.store_website;
		postObject.apiKey = $scope.store_api_key;
		postObject.apiPassword = $scope.store_api_password;
		postObject.appIcon = $scope.iconFileName;
		postObject.appSplash = $scope.splashFileName;
		postObject.bundleId = $scope.bundleId;

		 var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
		 if(isDevelopment)
    	    postObject.buildType = "development" ;
    	 else
    	    postObject.buildType = "appStore" ;

    	 if($scope.app_language == null)
    	    $scope.app_language = "en";

		postObject.certificate = $scope.certificateFileName;
		postObject.certificatePassword = $scope.passphrase;
		postObject.appLanguage = $scope.app_language;
		postObject.provisioningProfile =  provisioningProfileFileName;

		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};
		$scope.loading = true;
		$http.post(url, postObject, config).then(function(response){
		   // if()
			$scope.openApps();
			$scope.loading = false;
		});
	};

	$scope.uploadAndroidKeyStore = function(fileUrl, splashFileName){
		$scope.splashFileName = splashFileName;
		fileUpload.uploadFileToUrl($scope.androidKeyStore, $scope.createAndroidApp, fileUrl);
	};

    $scope.developmentAndroidApp = function(fileUrl){
        console.log('inside developmentAndroidBuild');
        $scope.iconFileName = "default";
        $scope.splashFileName = "default";
		$scope.bundleId = "default";
		$scope.androidKeyStore = "default";
		$scope.passphrase = "default";
		$scope.andoridAliasName = "default";
		$scope.createAndroidApp(fileUrl,$scope.androidKeyStore);
	};


	$scope.createAndroidApp = function(fileUrl, keyStoreFileName){
        console.log('inside createAndroidApp ');
		var url = "/AppifyCartAdmin/rest/admin/androidAppData.json";
        console.log('createApp android started');
        $scope.modal_box = true;
        $scope.modal_box_Production = true ;

		var postObject = new Object();
		postObject.accountEmail = localStorageService.get("currentuser");
		postObject.osType = "android";
		postObject.platform = $scope.platform;
		postObject.appName = $scope.name_of_app;
		postObject.website = $scope.store_website;
		postObject.apiKey = $scope.store_api_key;
		postObject.apiPassword = $scope.store_api_password;
		postObject.shopifyAccessToken = $scope.store_shopify_accessToken ;
		postObject.appIcon = $scope.iconFileName;
		postObject.appSplash = $scope.splashFileName;
		postObject.bundleId = $scope.bundleId;
		postObject.keyStoreFileName = keyStoreFileName;
		postObject.keyStorePassword = $scope.passphrase;
		postObject.aliasName = $scope.andoridAliasName;

		 var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
		 if(isDevelopment)
    	    postObject.buildType = "development" ;
    	 else
    	    postObject.buildType = "appStore" ;

    	 if($scope.app_language == null)
    	    $scope.app_language = "en";

        postObject.appLanguage = $scope.app_language;
		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};
		$scope.loading = true;
       //  console.log( JSON.stringify(postObject, null, "    ") );
		$http.post(url, postObject, config).then(function(response){
//           $scope.modal_box = false;
//           $scope.modal_box_Production = false ;
//		    if(response.data.status == "success"){
//                $scope.openApps();
//                $scope.loading = false;
//			} else {
//               //  $('.alert-build-failed', $('.form-horizontal')).show();
//			}
			$scope.openApps();
			$scope.loading = false;
		});
	};

	$scope.openHome = function(){
		$window.location.href = '/admin/DevelopmentAppCreator.html';    // before '/admin/AppifyMain.html';
	};
	$scope.openApps = function(){
	//buildingAppMessage
	    localStorageService.set("build_status", "in_process");
    	$window.location.href = '/admin/MyApps.html';
    };

/* added by avnish */
	$scope.loadIconSplash = function(){
	    var ostype = localStorageService.get("currentos");
	    if(ostype == "android"){

	    }
	    else{
            var isDevelopment = (localStorageService.get("buildType") == "development")?true:false;
            if(!isDevelopment){
                var user = localStorageService.get("currentuser");
                var url = "/AppifyCartAdmin/rest/admin/appIconSplash.json/" + user +"/"+ ostype;
                $http.get(url).then(function(response){
                    $scope.iconSplash = response.data.iconSplash;
                    $scope.showImages();
                });
            }
		}
	}
	$scope.showImages = function(){
		var user = localStorageService.get("currentuser");
			if ($scope.iconSplash != null){
			var appIconFile = $scope.iconSplash[0].appIcon;
			var appSplashFile = $scope.iconSplash[0].appSplash;
			var appSplashFile_file = $scope.iconSplash[0].appSplash;

			var appIconImageURL = "/ShopifyConnect/rest/admin/image/" + user + "/" + appIconFile;
			$scope.appIconFile = appIconImageURL;

			var appIconImageURL = "/ShopifyConnect/rest/admin/image/" + user + "/" + appSplashFile;
			$scope.splashScreenFile = appIconImageURL;

			}
	}

	//$scope.loadIconSplash();

/* added by avnish ends here*/

});
app.directive('fileModel', ['$parse', function ($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function(){
				scope.$apply(function(){
					modelSetter(scope, element[0].files[0]);
				});
			});
		}
	};
}]);
app.service('fileUpload', ['$http', function ($http) {
	this.uploadFileToUrl = function(file, callback, uploadUrl){

		var fd = new FormData();
		fd.append('file', file);
		var config = {
				withCredentials: true,
				headers: {'Content-Type': undefined},
				transformRequest: angular.identity
		};
		$http.post(uploadUrl, fd, config).success(function(response){
			if (callback != null){
				callback(uploadUrl, response.file);
			}
		});
	}
}]);



