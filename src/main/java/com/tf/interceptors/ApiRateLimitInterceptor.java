package com.tf.interceptors;

import com.tf.ratelimiting.RateLimiter;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Log4j2
public class ApiRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;

    public ApiRateLimitInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ipAddress) && !rateLimiter.allowRequest(ipAddress)) {
            log.warn("Rate limit exceeded for IP address: {}", ipAddress);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please try again later.");
            return false;
        }
        return true;
    }
}
