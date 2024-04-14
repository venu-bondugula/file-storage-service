package com.tf.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.tf.services.JPAUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;


/**
 * Configuration class for Spring Security settings.
 */
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
    @Value("${jwt.key}")
    private String jwtSymmetricKey;

    /**
     * Creates a bean of type {@link WebSecurityCustomizer} to customize web
     * security configuration.
     * This method allows ignoring requests to the H2 Console for database
     * administration purposes.
     *
     * @return The web security customizer bean.
     */
    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/h2-console/**");
    }

    /**
     * Creates a bean of type {@link SecurityFilterChain} to configure the security
     * filter chain.
     * This method defines the security behavior for different requests, including
     * authentication, authorization,
     * and JWT handling.
     *
     * @param http The {@link HttpSecurity} object to configure
     *             security settings.
     * @return The security filter chain.
     * @throws Exception if an error occurs during the configuration.
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {
        return http
                .cors()
                .and()
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                                .anyRequest().authenticated())
                // .headers(headers -> headers.frameOptions(opt -> opt.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedMethods(List.of(CorsConfiguration.ALL));
        corsConfiguration.addAllowedOriginPattern(CorsConfiguration.ALL);
        corsConfiguration.setAllowedHeaders(List.of(CorsConfiguration.ALL));
        corsConfiguration.setMaxAge(1800L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    /**
     * Creates a bean of type {@link AuthenticationManager} for authenticating user
     * credentials.
     *
     * @param userDetailsService The custom {@link JPAUserDetailsService}
     *                           implementation for user details retrieval.
     * @param pwdEncoder         The {@link PasswordEncoder} to encode and verify
     *                           user passwords.
     * @return The authentication manager bean.
     */
    @Bean
    AuthenticationManager authenticationManager(JPAUserDetailsService userDetailsService,
                                                PasswordEncoder pwdEncoder) {
        var authProvider = new DaoAuthenticationProvider(pwdEncoder);
        authProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(authProvider);
    }

    /**
     * Creates a bean of type {@link PasswordEncoder} for encoding and verifying
     * user passwords.
     *
     * @return The password encoder bean.
     */
    @Bean
    PasswordEncoder pwdEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates a bean of type {@link JwtEncoder} for encoding JWT tokens.
     *
     * @return The JWT encoder bean.
     */
    @Bean
    JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSymmetricKey.getBytes()));
    }

    /**
     * Creates a bean of type {@link JwtDecoder} for decoding JWT tokens.
     *
     * @return The JWT decoder bean.
     */
    @Bean
    JwtDecoder jwtDecoder() {
        byte[] bytes = jwtSymmetricKey.getBytes();
        SecretKeySpec originalKey = new SecretKeySpec(bytes, 0, bytes.length, "RSA");
        return NimbusJwtDecoder.withSecretKey(originalKey).macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

}