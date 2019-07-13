package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/tbitem")
public class ItemController {
    @Reference
    private ItemSearchService itemSearchService;
    @RequestMapping("/itemsearch")
    public Map Search(@RequestBody Map searchMap){
        Map<String, Object> map = itemSearchService.search(searchMap);
        return map;
    }
}
