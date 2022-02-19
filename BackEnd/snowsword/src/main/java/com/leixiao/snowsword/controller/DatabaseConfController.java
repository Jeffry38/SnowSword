package com.leixiao.snowsword.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leixiao.snowsword.dao.DatabaseConfRepository;
import com.leixiao.snowsword.dao.WebshellRepository;
import com.leixiao.snowsword.model.DatabaseConf;
import com.leixiao.snowsword.model.Webshell;
import com.leixiao.snowsword.util.WebshellOperate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@CrossOrigin
@RestController
@RequestMapping("/databaseConf")
public class DatabaseConfController {
    @Autowired
    private WebshellRepository webshellRepository;
    @Autowired
    private DatabaseConfRepository databaseConfRepository;

    @PostMapping("/add")
    public JSONObject add(@RequestBody JSONObject jsonParam){
        int webshellId = (int) jsonParam.get("webshellId");
        String host = (String) jsonParam.get("host");
        int port = (int) jsonParam.get("port");
        String username = (String) jsonParam.get("username");
        String password = (String) jsonParam.get("password");
        String code = (String) jsonParam.get("code");

        JSONObject resultObject = new JSONObject();

        try{
            Webshell webshell = webshellRepository.getById(webshellId);
            DatabaseConf databaseConf = new DatabaseConf();
            databaseConf.setHost(host);
            databaseConf.setPort(port);
            databaseConf.setUsername(username);
            databaseConf.setPassword(password);
            databaseConf.setCode(code);
            databaseConf.setUpdate_time(new Date(System.currentTimeMillis()));
            webshell.addDatabase(databaseConf);
            resultObject.put("code",20000);
            resultObject.put("data",databaseConfRepository.save(databaseConf));
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/deleteById")
    public JSONObject deleteById(@RequestParam Integer id){
        JSONObject resultObject = new JSONObject();

        try{
            databaseConfRepository.deleteById(id);
            resultObject.put("code",20000);
            resultObject.put("data","success");
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/getByWebShellId")
    public JSONObject getByWebShellId(@RequestParam Integer id){
        JSONObject resultObject = new JSONObject();

        try{
            JSONObject dataObject = new JSONObject();
            Webshell webshell = webshellRepository.getById(id);
            dataObject.put("url",webshell.getUrl());
            dataObject.put("databases",databaseConfRepository.findDatabaseConfsByWebshell_Id(id));
            resultObject.put("code",20000);
            resultObject.put("data",dataObject);
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @PostMapping("/update")
    public JSONObject update(@RequestBody JSONObject jsonParam){
        int id = (int) jsonParam.get("id");
        String host = (String) jsonParam.get("host");
        int port = (int) jsonParam.get("port");
        String username = (String) jsonParam.get("username");
        String password = (String) jsonParam.get("password");
        String code = (String) jsonParam.get("code");

        JSONObject resultObject = new JSONObject();

        try{
            DatabaseConf databaseConf = databaseConfRepository.getById(id);

            databaseConf.setHost(host);
            databaseConf.setPort(port);
            databaseConf.setUsername(username);
            databaseConf.setPassword(password);
            databaseConf.setCode(code);
            databaseConf.setUpdate_time(new Date(System.currentTimeMillis()));

            databaseConfRepository.save(databaseConf);
            resultObject.put("code",20000);
            resultObject.put("data","success");
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @PostMapping("/testConnection")
    public JSONObject testConnection(@RequestBody JSONObject jsonParam){
        int webshellId = (int) jsonParam.get("webshellId");

        JSONObject resultObject = new JSONObject();

        try{
            Webshell webshell = webshellRepository.getById(webshellId);
            WebshellOperate webshellOperate = new WebshellOperate(webshell.getUrl(),webshell.getPassword());

            resultObject.put("code",20000);
            try {
                JSONArray result = webshellOperate.executeSQL(jsonParam,"","SELECT \"success\";",-1);
                if(result.toString().contains("success")){
                    resultObject.put("data",true);
                }else{
                    resultObject.put("data",false);
                    resultObject.put("message",result.toString());
                }
            }catch (Exception exception){
                resultObject.put("data",false);
                resultObject.put("message",exception.toString());
            }

        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/getDatabases")
    public JSONObject getDatabases(@RequestParam Integer id){   //获取数据库名列表
        JSONObject resultObject = new JSONObject();
        try{
            DatabaseConf databaseConf = databaseConfRepository.getById(id);
            WebshellOperate webshellOperate = new WebshellOperate(databaseConf.getWebshell().getUrl(),databaseConf.getWebshell().getPassword());
            JSONArray resultArray = webshellOperate.executeSQL( (JSONObject)JSONObject.toJSON(databaseConf), "","SHOW DATABASES",-1 );
            resultObject.put("code",20000);
            resultObject.put("data",resultArray);
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/getTables")
    public JSONObject getTables(@RequestParam Integer id, @RequestParam String dbname){
        JSONObject resultObject = new JSONObject();
        try{
            DatabaseConf databaseConf = databaseConfRepository.getById(id);
            WebshellOperate webshellOperate = new WebshellOperate(databaseConf.getWebshell().getUrl(),databaseConf.getWebshell().getPassword());
            JSONArray resultArray = webshellOperate.executeSQL( (JSONObject)JSONObject.toJSON(databaseConf), dbname,"SHOW TABLES",-1 );
            resultObject.put("code",20000);
            resultObject.put("data",resultArray);
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/getColumns")
    public JSONObject getColumns(@RequestParam Integer id, @RequestParam String dbname, @RequestParam String table){
        JSONObject resultObject = new JSONObject();
        try{
            DatabaseConf databaseConf = databaseConfRepository.getById(id);
            WebshellOperate webshellOperate = new WebshellOperate(databaseConf.getWebshell().getUrl(),databaseConf.getWebshell().getPassword());
            JSONArray resultArray = webshellOperate.executeSQL( (JSONObject)JSONObject.toJSON(databaseConf), dbname,"DESC `"+table+"`",-1 );
            resultObject.put("code",20000);
            resultObject.put("data",resultArray);
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }

    @PostMapping("/executeSQL")
    public JSONObject executeSQL(@RequestBody JSONObject jsonParam){
        int id = (int) jsonParam.get("id");
        String dbname = (String) jsonParam.get("dbname");
        String SQL = (String) jsonParam.get("SQL");
        int limit = (int) jsonParam.get("limit");

        JSONObject resultObject = new JSONObject();
        try{
            DatabaseConf databaseConf = databaseConfRepository.getById(id);
            WebshellOperate webshellOperate = new WebshellOperate(databaseConf.getWebshell().getUrl(),databaseConf.getWebshell().getPassword());
            JSONArray resultArray = webshellOperate.executeSQL( (JSONObject)JSONObject.toJSON(databaseConf), dbname,SQL,limit );
            resultObject.put("code",20000);
            resultObject.put("data",resultArray);
        }catch (Exception exception){
            resultObject.put("code",50000);
            resultObject.put("message",exception.toString());
        }

        return resultObject;
    }
}
