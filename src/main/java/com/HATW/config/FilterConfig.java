package com.HATW.config;

import com.HATW.util.AdminFilter;
import com.HATW.util.JwtUtil;
import com.HATW.service.UserService;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<Filter> adminFilter(UserService userService, JwtUtil jwtUtil) {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        AdminFilter adminFilter = new AdminFilter(userService, jwtUtil);

        registrationBean.setFilter(adminFilter);
        registrationBean.addUrlPatterns("/api/reports/admin/*", "/api/inquiry/admin/*");
        registrationBean.setOrder(1); // 필요한 경우 순서 지정: 필터 우선순위

        return registrationBean;
    }
}
