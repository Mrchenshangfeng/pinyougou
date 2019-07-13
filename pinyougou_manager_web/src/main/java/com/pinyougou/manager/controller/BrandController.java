package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 查询所有品牌
     * @return
     */
    @RequestMapping("/getAll")
    public List<TbBrand> findAllBrand(){
      return  brandService.findAllBrand();
    }
    @RequestMapping("/getPage")
    /**
     * 分页查询品牌
     */
    public PageResult findPage(Integer pageNum,Integer pageSize){
        return  brandService.findPage(pageNum,pageSize);
    }
    @RequestMapping("/save")
    /**
     *保存品牌
     */
    //客户传递的是json数据
    public Result save(@RequestBody TbBrand tbBrand){
        try {
            brandService.save(tbBrand);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false,"保存失败");
    }
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand){
        try {
            brandService.update(tbBrand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false,"修改失败");
    }
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        TbBrand tbBrand = brandService.findOne(id);
        return tbBrand;
    }
    @RequestMapping("/delete")
    public Result delete(Long []ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  new Result(false,"删除失败");
    }
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand tbBrand,Integer pageNum,Integer pageSize){
        PageResult page = brandService.findPage1(tbBrand, pageNum, pageSize);
        return page;
    }
    @RequestMapping("/selectOptionList")
    public  List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
