package com.leixiao.snowsword.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leixiao.snowsword.dao.CategoryRepository;
import com.leixiao.snowsword.dao.WebshellRepository;
import com.leixiao.snowsword.model.Category;
import com.leixiao.snowsword.model.Webshell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private WebshellRepository webshellRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/findAll")
    public JSONObject findAll() {   //以JSON字符串形式返回所有category
        JSONObject resultObject = new JSONObject();
        List<Category> webshells = categoryRepository.findAll();
        JSONArray dataObject = JSONObject.parseArray(JSONObject.toJSONString(webshells));
        JSONObject tmp = new JSONObject();
        tmp.put("id",0);
        tmp.put("name","全部数据");
        tmp.put("webshellCount",webshellRepository.count());
        dataObject.add(0,tmp);
        resultObject.put("code",20000);
        resultObject.put("data",dataObject);
        return resultObject;
    }

    @PostMapping("/add")
    public JSONObject add(@RequestBody JSONObject jsonParam){   //前端以JSON格式传入分类名，存到数据库
        String name = (String) jsonParam.get("name");
        JSONObject resultObject = new JSONObject();

        try{
            Category category = new Category();
            category.setName(name);

            resultObject.put("code",20000);
            resultObject.put("data",categoryRepository.save(category));
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/deleteById")
    public JSONObject deleteById(@RequestParam Integer id){ //删除指定ID的category
        JSONObject resultObject = new JSONObject();

        try{
            Category category = categoryRepository.getById(id);
            category.clearWebshell();   //先清除关联的webshell才能删除成功
            categoryRepository.deleteById(id);
            resultObject.put("code",20000);
            resultObject.put("data","success");
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/addWebshellById")
    public JSONObject addWebshellById(@RequestParam Integer webshellId, @RequestParam Integer categoryId){ //添加指定ID的webshell到指定ID的category
        JSONObject resultObject = new JSONObject();

        try{
            Webshell webshell = webshellRepository.getById(webshellId);
            Category category = categoryRepository.getById(categoryId);
            category.addWebshell(webshell);
            webshell.addCategory(category); //必须在彼此定义对方类集合中添加，才会生效保存到数据库

            categoryRepository.save(category);
            resultObject.put("code",20000);
            resultObject.put("data","success");
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/removeWebshellById")
    public JSONObject removeWebshellById(@RequestParam Integer webshellId, @RequestParam Integer categoryId){ //从指定ID的category移除指定ID的webshell
        JSONObject resultObject = new JSONObject();

        try{
            Webshell webshell = webshellRepository.getById(webshellId);
            Category category = categoryRepository.getById(categoryId);
            category.removeWebshell(webshell);
            webshell.removeCategory(category);

            categoryRepository.save(category);
            resultObject.put("code",20000);
            resultObject.put("data","success");
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/renameById")
    public JSONObject renameById(@RequestParam Integer categoryId,@RequestParam String categoryName) { //更改指定ID的category的名称
        JSONObject resultObject = new JSONObject();

        try{
            Category category = categoryRepository.getById(categoryId);
            category.setName(categoryName);
            categoryRepository.save(category);
            resultObject.put("code",20000);
            resultObject.put("data","success");
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }
}
