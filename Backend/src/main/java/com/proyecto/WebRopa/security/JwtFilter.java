package com.proyecto.WebRopa.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.proyecto.WebRopa.repository.RegistrosRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtFilter extends GenericFilter {

    private final RegistrosRepository registrosRepository;
    private final JwtUtil jwtUtil;

    public JwtFilter(RegistrosRepository registrosRepository, JwtUtil jwtUtil) {
        this.registrosRepository = registrosRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest req,
            ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String header = request.getHeader("Authorization");
        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (jwtUtil.validarToken(token)) {
                    io.jsonwebtoken.Claims claims = jwtUtil.extraerClaims(token);
                    String userId = claims.getSubject();
                    Object empIdObj = claims.get("empresa_id");
                    Long empresaId = null;
                    if (empIdObj != null) {
                        if (empIdObj instanceof Number) {
                            empresaId = ((Number) empIdObj).longValue();
                        } else if (empIdObj instanceof String) {
                            try {
                                empresaId = Long.parseLong((String) empIdObj);
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                    }
                    java.util.List<String> permisos = claims.get("permisos", java.util.List.class);

                    TenantContext.setCurrentTenant(empresaId);

                    java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
                    if (permisos != null) {
                        for (String p : permisos) {
                            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(p));
                        }
                    }
                    String rol = claims.get("rol", String.class);
                    if (rol != null) {
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(rol));
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null,
                            authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }
}