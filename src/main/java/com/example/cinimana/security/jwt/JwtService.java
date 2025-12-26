package com.example.cinimana.security.jwt;

import com.example.cinimana.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Autowired
    private JwtConfig jwtConfig;

    // Clé de signature HS256
    private SecretKey getSigningKey() {//HS256 nécessite une clé secrète
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));//UTF_8 pour éviter les problèmes d'encodage
    }

    // Génération du token avec role, iat, exp, jti
    public String generateToken(UserDetails userDetails) {//il prend les info de l'utilisateur
        Map<String, Object> claims = new HashMap<>();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        claims.put("role", role.replace("ROLE_", ""));
        claims.put("jti", UUID.randomUUID().toString()); // ID unique pour le token
//iat indique quand le token est genere exp indique quand il expire
        return Jwts.builder() //builder() pour construire le token
                .setClaims(claims) //claims ajoute les informations personnalisées
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(getSigningKey())//signature du token avec la clé secrète
                .compact();//compact() génère le token final sous forme de chaîne
    }

    // Extraction username (email)
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Vérification token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
