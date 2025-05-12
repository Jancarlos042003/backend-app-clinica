package com.proyecto.appclinica.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtils {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private Long expirationTime;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public String createTokenFromUsername(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignature(), Jwts.SIG.HS256)
                .compact();
    }

    public String createTokenFromUsernameWithCustomClaims(String username, Map<String, Object> claims){
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignature(), Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSignature(), Jwts.SIG.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token){
        return getClaimFromToken(token, Claims::getSubject);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsTFunction){
        Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSignature())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean isTokenValid(String token){
        try {
            Jwts.parser()
                    .verifyWith(getSignature())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error validando el token JWT: {} - {}", e.getClass().getSimpleName(), e.getMessage());

            if (e instanceof ExpiredJwtException) {
                log.warn("Token expirado");
            } else if (e instanceof UnsupportedJwtException) {
                log.warn("Token no soportado");
            } else if (e instanceof MalformedJwtException) {
                log.warn("Token mal formado");
            } else if (e instanceof SignatureException) {
                log.warn("Firma no válida");
            } else if (e instanceof IllegalArgumentException) {
                log.warn("Token vacío o nulo");
            }

            return false;
        }
    }

    public SecretKey getSignature(){
        byte[] key = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }
}
