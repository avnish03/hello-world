    app.config(function (localStorageServiceProvider) {
        localStorageServiceProvider
        .setStorageType('sessionStorage');
    });

    app.controller('CategoryMapCtrl', function($scope, localStorageService, $cookies, $http, $window) {
        $scope.ostype = "ios";
        $scope.statusList = ["Enabled", "Disabled"];
        $scope.categoryMapsCopy = [];
        $scope.modal_box = false;
        $scope.loadCategoryData = function(){
          $scope.modal_box = true;
            if($scope.ostype == null){
                $scope.ostype = localStorageService.get("currentos");
            }

            $scope.categoryOptions = [];
            var user =  localStorageService.get("currentuser");
            var url = "/AppifyCartAdmin/rest/admin/appCategoryMaps.json/" + user + "/" + $scope.ostype;
            $http.get(url).then(function(response){
                if(response == null){
                    //
                }else if ("undefined" === typeof response.data)
                {
                    //
                }else {
                    $scope.categoryMaps = response.data.categoryMaps;

                    $scope.categoryMaps.forEach( function (item){
                        var option = Object();
                        option.id = item.categoryId;
                        option.name =  item.categoryName + "(" + item.categoryId + ")";
                        $scope.categoryOptions.push(option);
                    });


                     $scope.categoryOptions.forEach( function (item){
                         $scope.categoryMaps.forEach( function (it){
                           if(it.parentId == item.id ){
                             it.parent = item;
                           }
                         });
                     });

                     $scope.categoryMapsCopy = $scope.copyCategoryMaps($scope.categoryMaps);
                     $scope.modal_box = false;
                }
            });
        };


           $scope.saveMaps = function(){
               var url = "/AppifyCartAdmin/rest/admin/updateCategoryMaps.json";
               var config = {
                        headers: { 'Content-Type': 'application/json; charset=UTF-8'}
                };

                for(row in $scope.categoryMaps){
                      var option = $scope.categoryMaps[row].parent;
                      if (option != null){
                            $scope.categoryMaps[row].parentId = option.id;
                      }
                  }

                var postObject = new Object();
                postObject.user = localStorageService.get("currentuser");
                postObject.osType = $scope.ostype;
                postObject.categoryMaps = angular.toJson($scope.categoryMaps);
                $http.post(url, postObject, config).then(function(response){
                });
           };

           $scope.cancel = function(){
                $scope.categoryMaps = $scope.copyCategoryMaps($scope.categoryMapsCopy);
           }

           $scope.osChanged = function(osType){
             $scope.ostype = osType;
             localStorageService.set("currentos", osType);
             $scope.loadCategoryData();
           }

          $scope.copyCategoryMaps = function(source){
              var dest = [];

               source.forEach( function (item){
                              var newItem = Object();
                              newItem.categoryId = item.categoryId;
                              newItem.categoryName = item.categoryName;
                              newItem.parentId = item.parentId;
                              newItem.parent =  item.parent;
                              newItem.status =  item.status;
                              dest.push(newItem);
                });

              return dest;
          }
        $scope.loadCategoryData();
    });