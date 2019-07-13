app.controller("brandController",function ($scope,$controller,brandService) {
    $controller('baseController',{$scope:$scope});
    //查询品牌列表
    $scope.findAll=function () {
        brandService.findAll().success(function (response) {
            $scope.list=response;
        });
    };

    //分页
    $scope.findPage=function (currentPage,pageSize){
        brandService.findPage(currentPage,pageSize).success(function (response) {
            $scope.list=response.rows;
            //更新总记录
            $scope.paginationConf.totalItems=response.total;
        })
    };
    $scope.findOne=function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entry=response;
        })
    };
    //增加品牌
    $scope.save=function () {
        var object=null;
        if($scope.entry.id!=null){
            object=brandService.update($scope.entry)
        }else {
            object=brandService.add($scope.entry)
        }
        object.success(function (response) {
            if (response.success){
                //重新查询
                $scope.reloadList();//重新加载
            } else{
                alert(response.message)
            }
        })
    };


    //批量删除
    $scope.delete=function () {
        //获取选中的复选框
        if (confirm("你确定要删除么")) {
            brandService.delete($scope.selectIds).success(function (response) {
                if (response.success){
                    $scope.reloadList();//刷新列表
                }
            })
        }
    };
    //条件查询
    $scope.searchEntry={};
    $scope.search=function (pageNum,pageSize) {
        brandService.search(pageNum,pageSize,$scope.searchEntry).success(function (response) {
            $scope.list=response.rows;
            $scope.paginationConf.totalItems=response.total;
        })
    }
});