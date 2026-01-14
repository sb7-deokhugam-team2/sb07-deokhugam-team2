package com.deokhugam.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

public class MDCLoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID_HEADER = "Deokhugam-Request-ID";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String requestId = UUID.randomUUID().toString().substring(0, 8);

        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());

        response.setHeader(REQUEST_ID_HEADER, requestId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        MDC.clear();
    }
}
