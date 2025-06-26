package com.HATW.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // WebMvcConfigurer를 통한 CORS 설정 (Spring MVC의 DispatcherServlet이 처리)
    // 이 설정은 보통 Spring MVC 컨트롤러에 매핑되는 요청에 적용됩니다.
    // FilterRegistrationBean 방식이 더 강력하므로 이 부분을 제거해도 무방하지만,
    // 충돌이 발생하지 않으므로 같이 두어도 괜찮습니다.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 URL 경로에 대해 CORS 정책 적용
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173") // 프론트엔드 Origin 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 HTTP 헤더 허용 (Authorization, Content-Type 등)
                .allowCredentials(true)
                .maxAge(3600); // Preflight 요청 결과 캐싱 시간 (초단위, 1시간)
    }

    // 전역 CorsFilter를 직접 Bean으로 등록하여 필터 체인에서 가장 먼저 실행되도록 강제
    // 이 방식이 현재 CORS 문제를 해결할 가장 강력한 방법입니다.
    // 이 필터는 모든 HTTP 요청에 대해 CORS 헤더를 추가하려고 시도합니다.
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 허용할 Origin 목록. React 개발 서버 주소를 정확히 명시합니다.
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
        // 허용할 HTTP 메서드
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 허용할 헤더 목록. 모든 헤더를 허용하려면 "*"
        config.setAllowedHeaders(Arrays.asList("*"));
        // Preflight 요청 결과 캐싱 시간 (초단위)
        config.setMaxAge(3600L);

        // 모든 경로에 대해 이 CORS 설정을 적용
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        // 이 필터가 모든 다른 필터보다 가장 먼저 실행되도록 순서(order)를 0으로 설정
        bean.setOrder(0);
        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 실제 폴더 경로 (Windows는 file:/// 로 시작 + 역슬래시 말고 슬래시 사용)
        registry.addResourceHandler("/reportImgs/**")
                .addResourceLocations("file:///C:/Users/ljcho/Downloads/HowAboutThisWay_back-dev (1)/HowAboutThisWay_back-dev/uploads/reportImgs/");
    }
}
