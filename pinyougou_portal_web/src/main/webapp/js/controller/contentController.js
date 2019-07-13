app.controller('contentController',function ($scope,contentService) {
    $scope.item="12";

    $scope.contentList=[];//定义广告列表
    $scope.findContentByCategoryId=function (categoryId) {
        contentService.findContentByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId]=response;
        })
    }
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }

});