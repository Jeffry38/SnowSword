package com.leixiao.snowsword.controller;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.leixiao.snowsword.dao.UserRepository;
import com.leixiao.snowsword.model.User;
import com.leixiao.snowsword.util.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

//@CrossOrigin(origins = {"http://127.0.0.1:9528","http://localhost:9528"}) //允许这两个域的跨域请求
@CrossOrigin // 允许所有跨域请求
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository; // 框架自动注入userRepository对象，用来操作数据库user表
    private JwtToken jwtToken = new JwtToken();

    @PostMapping("/login")
    public JSONObject login(@RequestBody JSONObject jsonParam) {
        String username = (String) jsonParam.get("username");
        String password = (String) jsonParam.get("password");
        if(username.equals("admin") && userRepository.findUserByUsername(username)==null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setCreate_time(new Date(System.currentTimeMillis()));
            user.setUpdate_time(new Date(System.currentTimeMillis()));
            userRepository.save(user);
        }
        User user = userRepository.findUserByUsernameAndPassword(username, password);
        JSONObject resultObject = new JSONObject();
        if (user == null) {
            resultObject.put("code", 60204);
            resultObject.put("message", "username or password are incorrect.");
        } else {
            String token = jwtToken.createToken(username);
            JSONObject dataObject = new JSONObject();
            dataObject.put("token", token);
            resultObject.put("code", 20000);
            resultObject.put("data", dataObject);
        }
        return resultObject;
    }

    @GetMapping("/info")
    public JSONObject info(@RequestParam String token) {
        JSONObject resultObject = new JSONObject();
        try {
            String username = jwtToken.verifyToken(token);
            JSONObject dataObject = new JSONObject();
            dataObject.put("roles", new String[] { "admin" });
            dataObject.put("introduction", "I am a super administrator");
            dataObject.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
            dataObject.put("name", username);
            resultObject.put("code", 20000);
            resultObject.put("data", dataObject);
        } catch (JWTVerificationException jwtVerificationException) {
            resultObject.put("code", 50008);
            resultObject.put("message", "Login failed, unable to get user details.");
        }
        return resultObject;
    }

    @PostMapping("/logout")
    public JSONObject logout() {
        JSONObject resultObject = new JSONObject();
        resultObject.put("code", 20000);
        resultObject.put("data", "success");
        return resultObject;
    }
}
