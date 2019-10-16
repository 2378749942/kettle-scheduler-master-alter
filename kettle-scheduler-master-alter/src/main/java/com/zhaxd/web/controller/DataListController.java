package com.zhaxd.web.controller;

import com.zhaxd.web.service.DataListservice;
import com.zhaxd.web.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data/")
public class DataListController {

    @Autowired
    private DataListservice dataListService;

    @RequestMapping("getList.shtml")
    public String getList(Integer offset, Integer limit){
        return JsonUtils.objectToJson(dataListService.getList(offset, limit));
    }

    //data/getAllTb.shtml

    @RequestMapping("getAllTb.shtml")
    public String getAllTb(){
        return JsonUtils.objectToJson(dataListService.getAllTb());
    }
    @RequestMapping("getAllEx.shtml")
    public String getAllEx(){
        return JsonUtils.objectToJson(dataListService.getAllEx());
    }
    @RequestMapping("getAllSuccess.shtml")
    public String getAllSuccess(){
        return JsonUtils.objectToJson(dataListService.getAllSuccess());
    }



}
