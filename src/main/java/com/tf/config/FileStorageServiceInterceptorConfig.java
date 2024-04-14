package com.tf.config;

import com.tf.interceptors.ApiExecutionTimeInterceptor;
import com.tf.interceptors.ApiRateLimitInterceptor;
import com.tf.interceptors.RequestIdInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@AllArgsConstructor
public class FileStorageServiceInterceptorConfig implements WebMvcConfigurer {
    final RequestIdInterceptor requestIdInterceptor;
    final ApiExecutionTimeInterceptor apiExecutionTimeInterceptor;
    final ApiRateLimitInterceptor apiRateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestIdInterceptor);
        registry.addInterceptor(apiExecutionTimeInterceptor);
        registry.addInterceptor(apiRateLimitInterceptor);
    }
}
