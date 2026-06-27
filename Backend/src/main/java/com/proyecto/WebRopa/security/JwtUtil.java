package com.proyecto.WebRopa.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
@Component
public class JwtUtil {
    // Clave estática para evitar que las sesiones se invaliden al reiniciar el servidor
    private final Key key = Keys.hmacShaKeyFor("WebRopaSecretKeyParaTokensJWT1234567890".getBytes());
    private final long EXPIRATION_TIME = 100L*365*24*60*60*1000; // 100 años en milisegundos
    public String generarToken(String usuarioId, Long empresaId, java.util.List<String> permisos, String rol) {
        return Jwts.builder()
                .setSubject(usuarioId)
                .claim("empresa_id", empresaId)
                .claim("permisos", permisos)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key).compact();
    }

    public io.jsonwebtoken.Claims extraerClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key)
                                .build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public String extraerUsuarioId(String token) {
        return Jwts.parserBuilder().setSigningKey(key)
                                .build().parseClaimsJws(token)
                                .getBody().getSubject();
    }
}
