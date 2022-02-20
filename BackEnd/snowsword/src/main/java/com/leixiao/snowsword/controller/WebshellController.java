package com.leixiao.snowsword.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leixiao.snowsword.dao.CategoryRepository;
import com.leixiao.snowsword.dao.WebshellRepository;
import com.leixiao.snowsword.model.Category;
import com.leixiao.snowsword.model.Info;
import com.leixiao.snowsword.model.Webshell;
import com.leixiao.snowsword.util.UrlParser;
import com.leixiao.snowsword.util.WebshellOperate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping("/webshell")
public class WebshellController {
    @Autowired
    private WebshellRepository webshellRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/findAll")
    public JSONObject findAll() { // 以JSON字符串形式返回所有webshell，返回的数据包括Webshell对象的所有可访问属性
        JSONObject resultObject = new JSONObject();
        List<Webshell> webshells = webshellRepository.findAll();
        JSONArray dataObject = JSONObject.parseArray(JSONObject.toJSONString(webshells));
        resultObject.put("code", 20000);
        resultObject.put("data", dataObject);
        return resultObject;
    }

    @GetMapping("/getById")
    public JSONObject getById(@RequestParam Integer id) { // 返回指定ID的webshell数据
        JSONObject resultObject = new JSONObject();
        try {
            Webshell webshell = webshellRepository.getById(id);
            JSONObject dataObject = (JSONObject) JSONObject.toJSON(webshell);
            resultObject.put("code", 20000);
            resultObject.put("data", dataObject);
        } catch (Exception exception) {
            resultObject.put("code", 50000);
            resultObject.put("message", "There is no data with ID equal to " + id.toString() + " .");
        }
        return resultObject;
    }

    @PostMapping("/add")
    public JSONObject add(@RequestBody JSONObject jsonParam) { // 前端以JSON格式传入webshell信息，存到数据库
        String url = (String) jsonParam.get("url");
        String password = (String) jsonParam.get("password");
        String note = (String) jsonParam.get("note");
        JSONObject resultObject = new JSONObject();

        try {
            Webshell webshell = new Webshell();
            webshell.setUrl(url);
            webshell.setPassword(password);
            webshell.setNote(note);
            webshell.setType("PHP"); // 目前只考虑PHP类型，所以这里直接默认PHP
            webshell.setCreate_time(new Date(System.currentTimeMillis()));
            webshell.setUpdate_time(new Date(System.currentTimeMillis())); // 创建时间和更新时间都是当前时间

            UrlParser urlParser = new UrlParser(url);
            Info info = new Info();
            info.setIp_address(urlParser.getIp());
            info.setAddress(urlParser.getAddress());
            info.setOs("UNKNOWN"); // webshell所在主机的操作系统等信息需要连接webshell后获取，现在先设置为UNKNOWN
            info.setVersion("UNKNOWN");
            info.setUser("UNKNOWN");
            info.setHostname("UNKNOWN");
            info.setScript_filename("UNKNOWN");
            info.setUpdate_time(new Date(System.currentTimeMillis()));

            webshell.setInfo(info);

            resultObject.put("code", 20000);
            resultObject.put("data", webshellRepository.save(webshell));
        } catch (Exception exception) {
            resultObject.put("code", 50000);
            resultObject.put("message", exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/deleteById")
    public JSONObject deleteById(@RequestParam Integer id) { // 删除指定ID的webshell，调用此方法在删除webshell的同时会删除对应的info（直接操作数据库删除webshell则不会删除对应的info）
        JSONObject resultObject = new JSONObject();

        try {
            webshellRepository.deleteById(id);
            resultObject.put("code", 20000);
            resultObject.put("data", "success");
        } catch (Exception exception) {
            resultObject.put("code", 50000);
            resultObject.put("message", exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/getByCategoryId")
    public JSONObject getByCategoryId(@RequestParam int categoryId) { // 返回指定categoryId的webshell
        JSONObject resultObject = new JSONObject();
        try {
            Category category = categoryRepository.getById(categoryId);
            Set<Webshell> webshells = category.getWebshells();
            JSONArray dataObject = JSONObject.parseArray(JSONObject.toJSONString(webshells));
            resultObject.put("code", 20000);
            resultObject.put("data", dataObject);
        } catch (Exception exception) {
            resultObject.put("code", 50000);
            resultObject.put("message", exception.toString());
        }
        return resultObject;
    }

    @PostMapping("/update")
    public JSONObject update(@RequestBody JSONObject jsonParam) {
        int id = (int) jsonParam.get("id");
        String url = (String) jsonParam.get("url");
        String password = (String) jsonParam.get("password");
        String note = (String) jsonParam.get("note");
        JSONObject resultObject = new JSONObject();

        try {
            Webshell webshell = webshellRepository.getById(id);
            String oldIP = new UrlParser(webshell.getUrl()).getIp();

            webshell.setUrl(url);
            webshell.setPassword(password);
            webshell.setNote(note);
            webshell.setUpdate_time(new Date(System.currentTimeMillis()));
            UrlParser urlParser = new UrlParser(url);
            if (!urlParser.getIp().equals(oldIP)) {
                Info info = webshell.getInfo();
                info.setIp_address(urlParser.getIp());
                info.setAddress(urlParser.getAddress());
                info.setOs("UNKNOWN");
                info.setVersion("UNKNOWN");
                info.setUser("UNKNOWN");
                info.setHostname("UNKNOWN");
                info.setScript_filename("UNKNOWN");
                info.setUpdate_time(new Date(System.currentTimeMillis()));
            }
            webshellRepository.save(webshell);
            resultObject.put("code", 20000);
            resultObject.put("data", "success");
        } catch (Exception exception) {
            resultObject.put("code", 50000);
            resultObject.put("message", exception.toString());
        }

        return resultObject;
    }

    @PostMapping("/testConnection")
    public JSONObject testConnection(@RequestBody JSONObject jsonParam) {
        String url = (String) jsonParam.get("url");
        String password = (String) jsonParam.get("password");
        JSONObject resultObject = new JSONObject();

        try {
            WebshellOperate webshellOperate = new WebshellOperate(url, password);
            resultObject.put("code", 20000);
            resultObject.put("data", webshellOperate.testConnection());
        } catch (Exception exception) {
            resultObject.put("code", 50000);
            resultObject.put("message", exception.toString());
        }

        return resultObject;
    }

    @GetMapping("/getAndUpdateInfoById")
    public JSONObject getAndUpdateInfoById(@RequestParam Integer id) throws IOException {
        JSONObject resultObject = new JSONObject();
        Webshell webshell = webshellRepository.getById(id);
        String url = webshell.getUrl();
        WebshellOperate webshellOperate = new WebshellOperate(url, webshell.getPassword());
        if (webshellOperate.testConnection()) {
            try {
                JSONObject infoObject = webshellOperate.getInfo();
                String user = infoObject.getString("user");
                String hostname = infoObject.getString("hostname");
                String version = infoObject.getString("version");
                String script_filename = infoObject.getString("script_filename");
                String os = infoObject.getString("os");
                UrlParser urlParser = new UrlParser(url);
                String ip_address = urlParser.getIp();
                String address = urlParser.getAddress();

                Info info = webshell.getInfo();
                info.setUser(user);
                info.setHostname(hostname);
                info.setVersion(version);
                info.setScript_filename(script_filename);
                info.setOs(os);
                info.setIp_address(ip_address);
                info.setAddress(address);
                info.setUpdate_time(new Date(System.currentTimeMillis()));
                webshellRepository.save(webshell);

                JSONObject dataObject = new JSONObject();
                dataObject.put("url", url);
                dataObject.put("user", user);
                dataObject.put("hostname", hostname);
                dataObject.put("version", version);
                dataObject.put("script_filename", script_filename);
                dataObject.put("os", os);
                dataObject.put("ip_address", ip_address);
                dataObject.put("address", address);

                resultObject.put("code", 20000);
                resultObject.put("data", dataObject);
            } catch (Exception exception) {
                resultObject.put("code", 50000);
                resultObject.put("message", exception.toString());
            }
        } else {
            resultObject.put("code", 50000);
            resultObject.put("message", "webshell connection failed");
        }

        return resultObject;
    }

    @GetMapping("/getProcessListById")
    public JSONObject getProcessListById(@RequestParam Integer id) throws IOException {
        JSONObject resultObject = new JSONObject();
        Webshell webshell = webshellRepository.getById(id);
        WebshellOperate webshellOperate = new WebshellOperate(webshell.getUrl(), webshell.getPassword());
        if (webshellOperate.testConnection()) {
            try {
                resultObject.put("code", 20000);
                resultObject.put("data", webshellOperate.getProcessList());
            } catch (Exception exception) {
                resultObject.put("code", 50000);
                resultObject.put("message", exception.toString());
            }
        } else {
            resultObject.put("code", 50000);
            resultObject.put("message", "webshell connection failed");
        }

        return resultObject;
    }

    @PostMapping("/executeCommandById")
    public JSONObject executeCommandById(@RequestBody JSONObject jsonParam) throws IOException {
        int id = (int) jsonParam.get("id");
        String path = (String) jsonParam.get("path");
        String command = (String) jsonParam.get("command");

        JSONObject resultObject = new JSONObject();
        Webshell webshell = webshellRepository.getById(id);
        WebshellOperate webshellOperate = new WebshellOperate(webshell.getUrl(), webshell.getPassword());
        if (webshellOperate.testConnection()) {
            try {
                resultObject.put("code", 20000);
                resultObject.put("data", webshellOperate.executeCommand(path, command));
            } catch (Exception exception) {
                resultObject.put("code", 50000);
                resultObject.put("message", exception.toString());
            }
        } else {
            resultObject.put("code", 50000);
            resultObject.put("message", "webshell connection failed");
        }

        return resultObject;
    }

    @GetMapping("/getTerminalInfoById")
    public JSONObject getTerminalInfoById(@RequestParam Integer id) throws IOException {
        JSONObject resultObject = new JSONObject();
        Webshell webshell = webshellRepository.getById(id);
        WebshellOperate webshellOperate = new WebshellOperate(webshell.getUrl(), webshell.getPassword());
        if (webshellOperate.testConnection()) {
            try {
                JSONObject dataObject = webshellOperate.getInfo();
                dataObject.put("url", webshell.getUrl());

                resultObject.put("code", 20000);
                resultObject.put("data", dataObject);
            } catch (Exception exception) {
                resultObject.put("code", 50000);
                resultObject.put("message", exception.toString());
            }
        } else {
            resultObject.put("code", 50000);
            resultObject.put("message", "webshell connection failed");
        }

        return resultObject;
    }

    @PostMapping("/loadFilesById")
    public JSONObject loadFilesById(@RequestBody JSONObject jsonParam) {
        int id = (int) jsonParam.get("id");
        String path = (String) jsonParam.get("path");

        JSONObject resultObject = new JSONObject();
        Webshell webshell = webshellRepository.getById(id);
        WebshellOperate webshellOperate = new WebshellOperate(webshell.getUrl(), webshell.getPassword());

        try {
            resultObject.put("code", 20000);
            resultObject.put("data", webshellOperate.loadFiles(path));
        } catch (Exception exception) {
            resultObject.put("code", 50000);
            resultObject.put("message", exception.toString());
        }

        return resultObject;
    }

    @PostMapping("/previewFileById")
    public JSONObject previewFileById(@RequestBody JSONObject jsonParam) {
        int id = (int) jsonParam.get("id");
        String path = (String) jsonParam.get("path");
        int txtLimit = (int) jsonParam.get("txtLimit");
        int imgLimit = (int) jsonParam.get("imgLimit");

        JSONObject resultObject = new JSONObject();
        Webshell webshell = webshellRepository.getById(id);
        WebshellOperate webshellOperate = new WebshellOperate(webshell.getUrl(), webshell.getPassword());
        try {
            resultObject.put("code", 20000);
            resultObject.put("data", webshellOperate.previewFile(path, txtLimit, imgLimit));
        } catch (Exception exception) {
            resultObject.put("code", 50000);
            resultObject.put("message", exception.toString());
        }

        return resultObject;
    }
}
