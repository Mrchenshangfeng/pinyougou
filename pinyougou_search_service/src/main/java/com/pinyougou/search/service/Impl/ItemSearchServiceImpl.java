package com.pinyougou.search.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Override
    public Map<String, Object> search(Map searchMap) {
        //关键字处理
        String keywords = (String) searchMap.get("keywords");
        keywords =  keywords.replace(" ","");

        if(keywords.equals("")) {
            keywords = " ";
        }
        searchMap.put("keywords",keywords);
        Map<String,Object> map = new HashMap();
        //查询列表 (高亮显示)
        map.putAll(searchList(searchMap));
        //按照关键字查询商品分类
        List <String>categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //查询品牌和规格列表
        String categoryName = (String) searchMap.get("categoryName");
        if (!categoryName.equals("")){
            Map brandAndSpecList = getBrandAndSpecList(categoryName);
            map.putAll(brandAndSpecList);
        }else {//如果没有分类名称，按照第一个查询
            if(categoryList!=null&&categoryList.size()>0){
                Map brandAndSpecList = getBrandAndSpecList(categoryList.get(0));
                map.putAll(brandAndSpecList);
            }
        }


        /*Query query=new SimpleQuery("*:*");
        //添加查询条件
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page= solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows",page.getContent());*/
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 根据关键字搜索列表
     * @param
     * @return
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();//创建查询条件
        //设置高亮显示的域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");//设置高亮前缀
        highlightOptions.setSimplePostfix("</em>");//设置高亮显示的后缀
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //按分类分类筛选
        if (!"".equals(searchMap.get("categoryName"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("categoryName"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //按品牌筛选
        if (!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //按规格分类查询
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }
        //按照价格过滤
        if (!"".equals(searchMap.get("price"))){
            String [] price = ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")){//最小值不等于0
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")){//最大值不等于*
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");//提取页码
        if (pageNo==null){
            pageNo=1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//提取每页记录数
        if (pageSize==null){
            pageSize=20;//默认20条
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        //价格排序
        String sortValue= (String) searchMap.get("sort");//ASC DESC
        String sortField= (String) searchMap.get("sortField");
        if (sortValue!=null &&!sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }
        //按关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        for (HighlightEntry<TbItem> tbItemHighlightEntry : page.getHighlighted()) {
            //循环定高亮的入口的集合
            TbItem item = tbItemHighlightEntry.getEntity();//原实体类
            if (tbItemHighlightEntry.getHighlights().size()>0 && tbItemHighlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                item.setTitle(tbItemHighlightEntry.getHighlights().get(0).getSnipplets().get(0));//高亮结果
            }
        }

        map.put("rows",page.getContent());
        System.out.println(page.getContent().size());
        map.put("totalPages",page.getTotalPages());//总页数
        map.put("total",page.getTotalElements());//总记录数

        return  map;
    }
    /**
     * 从缓存中获得品牌列表和规格列表
     *
     */
    @Autowired
    private RedisTemplate redisTemplate;

    private   Map getBrandAndSpecList(String categoryName){
        Long templateId= (Long) redisTemplate.boundHashOps("itemCat").get(categoryName);
        Map map = new HashMap();
        if (templateId!=null){
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(templateId);
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("brandList",brandList);
            map.put("specList",specList);

        }
        return map;
    }

    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap){
        List<String>list = new ArrayList<>();
        SimpleQuery query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page= solrTemplate.queryForGroupPage(query,TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果的入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            list.add(entry.getGroupValue());//将分组结果的名臣封装到返回值中
        }
        return list;
    }

}
