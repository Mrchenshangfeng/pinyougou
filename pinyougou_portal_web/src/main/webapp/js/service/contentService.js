app.service('contentService',function ($http) {
    //根据广告分类id查询 列表
    this.findContentByCategoryId=function (categoryId) {
        return $http.get('content/findByCategoryId.do?categoryId='+categoryId);
    }
});