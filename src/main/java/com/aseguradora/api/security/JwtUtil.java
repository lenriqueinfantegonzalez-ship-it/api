package com.aseguradora.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Clave secreta para firmar los tokens (debe ser larga y segura)
    // En un proyecto real esto iría en application.properties
    private static final String SECRET_STRING = "esta_es_una_clave_secreta_super_segura_y_muy_larga_para_funcionar_con_hs256_12345";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    // Duración del token: 10 horas (en milisegundos)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; 

    // --- GENERAR TOKEN ---
    // Recibe correo, rol e ID para guardarlos dentro del token
    public String generateToken(String username, String role, Long idUsuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", role);
        claims.put("idUsuario", idUsuario);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // --- VALIDAR TOKEN ---
    public Boolean validateToken(String token, String username) {
        final String usernameDelToken = extractUsername(token);
        return (usernameDelToken.equals(username) && !isTokenExpired(token));
    }

    // --- EXTRAER DATOS ---
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}