package com.aguilar.luisr.springsecurity.config;

import com.aguilar.luisr.springsecurity.security.JwtFilter;
import com.aguilar.luisr.springsecurity.security.LoginRateLimitFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final LoginRateLimitFilter loginRateLimitFilter;

    private final JwtFilter jwtFilter;

    public SecurityConfiguration(LoginRateLimitFilter loginRateLimitFilter, JwtFilter jwtFilter) {
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // API stateless (si usas cookies cambia esto)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)      // <-- quita /login
                .httpBasic(AbstractHttpConfigurer::disable)    // <-- quita Basic Auth
                /*.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())*/
                .authorizeHttpRequests(auth -> auth
                        // permitir los endpoints de autenticación (ajusta según tu controller)
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/api/v1/auth/**",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // permitir preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            LOG.debug("AuthenticationEntryPoint -> 401 for path={}", req.getRequestURI());
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            LOG.debug("AccessDeniedHandler -> 403 for path={}, user={}", req.getRequestURI(), req.getRemoteUser());
                            res.sendError(HttpServletResponse.SC_FORBIDDEN);
                        })

                )
                // Rate limit lo más temprano posible con un filtro de orden conocido por Spring Security
                .addFilterBefore(loginRateLimitFilter, SecurityContextHolderFilter.class)
                // JWT antes del UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // expone AuthenticationManager para el controlador de login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}
