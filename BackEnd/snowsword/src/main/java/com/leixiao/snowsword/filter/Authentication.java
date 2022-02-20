package com.leixiao.snowsword.filter;

import com.leixiao.snowsword.util.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@WebFilter(filterName = "authFilter", urlPatterns = "/*")
public class Authentication implements Filter {
    @Autowired
    private JwtToken jwtToken;

    private static String[] whitePaths = {
            "/user/login",
            "/user/logout"
    };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String servletPath = request.getServletPath();
        if(Arrays.asList(whitePaths).contains(servletPath)){
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        if("OPTIONS".equals(request.getMethod())){
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }


        Cookie[] cookies = request.getCookies();
        String token = request.getHeader("X-Token");
        if(token == null){
            token = getCookie(cookies,"snow_sword_token");
        }
        if(token == null){
            token = request.getParameter("token");
        }

        if(token != null && auth(token)){
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }else{
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.sendError(403);
        }


    }

    private String getCookie(Cookie[] cookies,String key){
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
