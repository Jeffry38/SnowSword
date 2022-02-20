package com.leixiao.snowsword.configurer;

import com.leixiao.snowsword.interceptor.Authentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    Authentication AuthenticationInterceptor(){
        return new Authentication();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(AuthenticationInterceptor()).addPathPatterns("/**")
        .excludePathPatterns("/user/login")
        .excludePathPatterns("/user/logout")
        .excludePathPatterns("/error")
        .excludePathPatterns("/index.html")
        .excludePathPatterns("/")
        .excludePathPatterns("/static/**");
    }
}
