//服务层
app.service('brandService',function ($http) {
    this.findAll=function () {
        return $http.get('../brand/findAll.do');
    };
    this.findPage=function (currentPage, pageSize) {
        return  $http.get('../brand/getPage.do?pageNum='+currentPage+'&pageSize='+pageSize)
    };
    this.findOne=function (id) {
        return  $http.get('../brand/findOne.do?id='+id)
    };
    this.add=function (entry) {
        return $http.post('../brand/save.do',entry)
    };
    this.update=function (entry) {
        return  $http.post('../brand/update.do',entry)
    };
    this.delete=function (ids) {
        return   $http.get('../brand/delete.do?ids='+ids)
    };
    this.search=function (pageNum, pageSize,searchEntry) {
        return $http.post('../brand/search.do?pageNum='+pageNum+'&pageSize='+pageSize,searchEntry)
    }
    //下拉列表数据
    this.selectOptionList=function () {
        return $http.get('../brand/selectOptionList.do');
    }
});