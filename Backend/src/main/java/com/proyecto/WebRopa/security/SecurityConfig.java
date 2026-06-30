package com.proyecto.WebRopa.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http, JwtFilter jwtFilter)
        throws Exception 
        {
            http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    // Ya no se sirven archivos estáticos desde el backend
                    .requestMatchers("/api/token", "/api/registros").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/usuarios/login", "/api/usuarios/registro").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/usuarios/me").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**", "/api/categorias", "/api/categorias/**", "/api/empresas", "/api/empresas/**", "/api/sucursales", "/api/sucursales/**").permitAll()
                    .anyRequest().authenticated())
                    .addFilterBefore(jwtFilter,
                    UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(List.of("*"));
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder(){
            return new BCryptPasswordEncoder();
        }
    
}

