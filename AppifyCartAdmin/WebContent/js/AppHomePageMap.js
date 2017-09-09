    app.config(function (localStorageServiceProvider) {
        localStorageServiceProvider
        .setStorageType('sessionStorage');
    });

    app.controller('AppHomePageMapCtrl', function($scope, localStorageService, $cookies, $http, $window) {
        $scope.ostype = "ios";
        $scope.statusList = ["Enabled", "Disabled"];
        $scope.appHomePageRows = [];
        $scope.categoryMapsCopy = [];
        $scope.modal_box = false;
        $scope.loadCategoryData = function(){

          $scope.modal_box = true;
            if($scope.ostype == null){
                $scope.ostype = localStorageService.get("currentos");
            }

            $scope.categoryOptions = [];
            var user =  localStorageService.get("currentuser");
            var url = "/AppifyCartAdmin/rest/admin/appHomepageSections.json/" + user + "/" + $scope.ostype;
            $http.get(url).then(function(response){
            $scope.categoryMaps=[];
            $scope.appHomePageRows = [];
            $scope.currentCategoryMaps=[];
            $scope.categoryMapsCopy=[];
                // console.log( JSON.stringify(response.data, null, "    ") );
                if(response == null){
                    //
                }else if ("undefined" === typeof response.data)
                {
                    //
                }else {
                    $scope.categoryMaps = response.data.result.allCategories.collections;
                    $scope.currentCategoryMaps = response.data.result.mappings;
                    // console.log( JSON.stringify($scope.currentCategoryMaps, null, "    ") );
                    $scope.currentCategoryMaps.forEach( function (item){
                        // console.log('Matched : item'+item + ' ; item.section : '+item.section);
                        var sectionTemp = Object();
                        sectionTemp.sectionName = item.sectionName;
                        sectionTemp.section = item.section;
                        sectionTemp.sectionMapId = item.sectionMapId;
                        sectionTemp.status = item.status;
                        sectionTemp.name =  sectionTemp.sectionMapId + "(" + sectionTemp.sectionName + ")";

                        $scope.selectedCategory = sectionTemp.name;
                        $scope.appHomePageRows.push(sectionTemp);
                    });

                    $scope.categoryMaps.forEach( function (item){

                        var option = Object();
                        option.id = item.id;
                        option.title = item.title;
                        option.name =  item.id + "(" + item.title + ")";
                        $scope.categoryOptions.push(option);
                    });

                    $scope.categoryMapsCopy = $scope.copyCategoryMaps($scope.currentCategoryMaps);
                    $scope.processingStatus="";
                    $scope.modal_box = false;
                }
            });
        };

/**
  *     Maps the callections to the sections of the homepage in mobile app
 **/
        $scope.save = function(){
            $scope.processingStatus="Processing . . . Please wait";
            var url = "/AppifyCartAdmin/rest/admin/updateAppHomepageSections.json";
            var config = {
                headers: { 'Content-Type': 'application/json; charset=UTF-8'}
            };

            var postObject = new Object();
            postObject.user = localStorageService.get("currentuser");
            postObject.osType = $scope.ostype;
            postObject.categoryMaps = angular.toJson($scope.currentCategoryMaps);
            console.log('to  be  updated ');
             console.log( JSON.stringify(postObject, null, "    ") );
            $http.post(url, postObject, config).then(function(response){
                $scope.processingStatus="Saved Successfully";
                $scope.loadCategoryData();
            });
        };

        $scope.selectedCategoryChanged = function(sourceItem,selected){
            $scope.currentCategoryMaps.forEach( function (item){
                if(item.section === sourceItem.section){
                    item.sectionMapId = selected.id;
                    item.section = sourceItem.section;
                    item.sectionName = selected.title;
                    item.status = sourceItem.status;
                }
            });
        }

        $scope.selectedStatusChanged = function(sourceItem,selected){
                    console.log('sourceItem'+ JSON.stringify(sourceItem, null, "    ") );
                    console.log('selected '+ JSON.stringify(selected, null, "    ") );

            $scope.currentCategoryMaps.forEach( function (item){
                if(item.section === sourceItem.section){
                    item.status = sourceItem.status;
                }
            });
            console.log('after changed');
            console.log( JSON.stringify($scope.currentCategoryMaps, null, "    ") );
        }

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
            var sectionTemp = Object();
            sectionTemp.sectionName = item.sectionName;
            sectionTemp.section = item.section;
            sectionTemp.sectionMapId = item.sectionMapId;
            sectionTemp.name =  sectionTemp.sectionMapId + "(" + sectionTemp.sectionName + ")";
            dest.push(sectionTemp);
        });

              return dest;
          }
        $scope.loadCategoryData();
    });