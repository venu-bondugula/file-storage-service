package com.tf.ratelimiting;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import jakarta.annotation.Nonnull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class RateLimiter {
    private final LoadingCache<String, Long> requestCounts;
    private final long limit;      // Maximum allowed requests within the time window

    public RateLimiter(@Value("${ratelimit.time-window}") long timeWindow, @Value("${ratelimit.limit}") long limit) {
        // Time window in milliseconds (e.g., 60000 for 1 minute)
        this.limit = limit;
        requestCounts = CacheBuilder.newBuilder()
                .expireAfterWrite(timeWindow, TimeUnit.MILLISECONDS)
                .build(new RequestCountLoader());
    }

    public boolean allowRequest(String identifier) {

        try {
            long currentCount = requestCounts.getUnchecked(identifier);
            return currentCount < limit;
        } catch (UncheckedExecutionException e) {
            log.warn("Cache unavailable for rate limiting. Request allowed but may exceed limit for {}", identifier);
            return true; // Allow request with logging (example fallback)
        }
    }

    private static class RequestCountLoader extends CacheLoader<String, Long> {
        @Nonnull
        @Override
        public Long load(@Nonnull String key) {
            return 0L; // Initial value for the request count
        }
    }
}
