package com.pinyougou.sellergoods.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    List<TbBrand> findAllBrand();
   PageResult findPage(Integer pageNum, Integer pageSize);
    void save(TbBrand tbBrand);
    void update(TbBrand tbBrand);
    TbBrand findOne(Long id);
    void delete(Long []ids);
    PageResult findPage1(TbBrand tbBrand,Integer pageNum,Integer pageSize);

    /**
     * 返回下拉列表数据
     * @return
     */
    List<Map> selectOptionList();
}
