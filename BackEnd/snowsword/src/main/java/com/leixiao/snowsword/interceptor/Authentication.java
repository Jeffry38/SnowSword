package com.leixiao.snowsword.interceptor;

import com.leixiao.snowsword.util.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Authentication implements HandlerInterceptor {
    @Autowired
    private JwtToken jwtToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = request.getHeader("X-Token");
        if(token == null){
            token = getCookie(cookies,"snow_sword_token");
        }
        if(token == null){
            token = request.getParameter("token");
        }

        if(token != null && auth(token)){
            return HandlerInterceptor.super.preHandle(request, response, handler);
        }else{
            response.sendError(403);
            return false;
        }

    }

    private String getCookie(Cookie[] cookies, String key){
        if(cookies == null){
            return null;
        }
        for(Cookie cookie : cookies){
            if (key.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private Boolean auth(String token){
        try{
            jwtToken.verifyToken(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
