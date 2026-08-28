package com.campus.market.config;

import com.campus.market.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
    private final List<String> allowedOrigins;
    public SecurityConfig(@Value("${app.cors-allowed-origins}") List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(allowedOrigins); c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("*")); c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/**",c); return s;
    }
    @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwt) throws Exception {
        return http.csrf(x->x.disable()).cors(x->{}).sessionManagement(x->x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(x->x.frameOptions(f->f.sameOrigin()))
                .exceptionHandling(x->x
                        .authenticationEntryPoint((request,response,error)->writeError(response,401,40101,"请先登录"))
                        .accessDeniedHandler((request,response,error)->writeError(response,403,40301,"无权限执行此操作")))
                .authorizeHttpRequests(x->x
                        .requestMatchers("/api/auth/**","/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/products/**","/api/categories","/api/users/*/reviews","/uploads/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }
    private static void writeError(HttpServletResponse response,int status,int code,String message) throws IOException {
        response.setStatus(status); response.setCharacterEncoding("UTF-8"); response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":"+code+",\"message\":\""+message+"\",\"data\":null}");
    }
}
