package com.api.config;

import com.api.security.JwtAccessDeniedHandler;
import com.api.security.JwtAuthenticationEntryPoint;
import com.api.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * Class SecurityConfig
 *
 * Security configuration class that sets up HTTP security, JWT-based authentication, and CORS policies.
 * This class is responsible for configuring access control, enabling JWT filtering, and defining allowed HTTP methods and headers for cross-origin requests.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * Configures the HTTP security settings for the application.
     * - Disables basic authentication and CSRF protection.
     * - Configures session management to be stateless (no session state is maintained on the server).
     * - Defines authorized URLs that are publicly accessible.
     * - Adds the JWT filter after the `UsernamePasswordAuthenticationFilter` to intercept requests.
     * - Configures CORS settings for handling cross-origin requests.
     *
     * @param http the HttpSecurity object used to customize the security settings.
     * @throws Exception if an error occurs during configuration.
     */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(
                            auth -> auth
                                    .requestMatchers(
                                            "/auth/login", "/auth/newAccessToken",
                                            "/v1/bank-cards-management-api-docs/**", "/swagger-ui/**", "/v1/bank-cards-management-api-docs")
                                    .permitAll()
                                    .anyRequest().authenticated()
                    )
                    .addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                    .cors((cors) -> cors.configurationSource(corsConfigurationSource()))
                    .exceptionHandling((exceptions) -> exceptions
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint) // to handle jwt exceptions in the header
                            .accessDeniedHandler(jwtAccessDeniedHandler) // to handle admin role exception
                    )
                    .build();
        }

    /**
     * Configures the CORS (Cross-Origin Resource Sharing) settings for the application.
     * This method sets the allowed origins, HTTP methods, headers, and credentials for cross-origin requests.
     * It allows requests from any origin and supports common HTTP methods like GET, POST, PUT, and DELETE.
     */
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); //frontend URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

