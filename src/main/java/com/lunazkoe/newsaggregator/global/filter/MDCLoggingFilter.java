package com.lunazkoe.newsaggregator.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 모든 필터 중 가장 먼저 실행
public class MDCLoggingFilter extends OncePerRequestFilter {

    private final static String REQUEST_ID = "requestId";
    private final String REQUEST_METHOD = "requestMethod";
    private final String REQUEST_URL = "requestUrl";
    private final String CLIENT_IP = "clientIp";
    private final String USER_ID = "userId";
    private final String HEADER_REQUEST_ID = "X-Request-Id";
    public final static String HEADER_USER_ID = "MoNew-Request-User-ID"; // 기획안 명세 반영

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            String userId = request.getHeader(HEADER_USER_ID);

            MDC.put(REQUEST_ID, requestId);
            MDC.put(REQUEST_METHOD, request.getMethod());
            MDC.put(REQUEST_URL, request.getRequestURI());
            MDC.put(CLIENT_IP, extractClientIp(request));

            if (userId != null && !userId.isBlank()) {
                MDC.put(USER_ID, userId);
            }

            response.addHeader(HEADER_REQUEST_ID, requestId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    // 프록시 환경(AWS, Nginx 등)을 고려한 클라이언트 IP 추출 메서드
    // - 운영환경에서는 사용자가 Spring Boot 서버로 직접 연결되지 않음
    // - 중간에 거쳐 온 프록시(로드 밸런서)의 IP가 찍히게 됨
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        // - 프록시를 거치지 않은 경우 (개발 환경)
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 다중 프록시를 거친 경우 첫 번째 IP가 실제 클라이언트 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}
