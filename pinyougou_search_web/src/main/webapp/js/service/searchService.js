app.service('searchService',function($http){

this.search=function (searchMap) {
    return $http.post('tbitem/itemsearch.do',searchMap);
}

});