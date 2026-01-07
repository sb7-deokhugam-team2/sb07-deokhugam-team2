package com.deokhugam.global.config;

import com.deokhugam.global.interceptor.MDCLoggingInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MDCLoggingInterceptor())
                .addPathPatterns("/**");
    }
}
