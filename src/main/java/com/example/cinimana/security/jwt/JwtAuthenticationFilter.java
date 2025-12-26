// src/main/java/com/example/cinimana/security/jwt/JwtAuthenticationFilter.java
package com.example.cinimana.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.cinimana.security.user.CustomUserDetailsService;

import java.io.IOException;
//À chaque requête protégée, le `JwtAuthenticationFilter` entre en jeu :
//Extraction : Il récupère le header `Authorization: Bearer <token>`.
//Validation : Il utilise la clé secrète (définie dans `application.properties`) pour vérifier l'intégrité du jeton.
//Injection : Si le token est valide, le filtre "injecte" l'utilisateur dans le `SecurityContextHolder`.
//Autorisation : Spring Security vérifie ensuite si l'utilisateur injecté possède le rôle requis pour l'URL demandée.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extraction du token
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        //  email sera NULL si le token est invalide/expiré/mauvaise signature
        String email = jwtService.extractUsername(token);

        // 2. Vérification et chargement de l'utilisateur
        // On procède SEULEMENT si l'email a été extrait ET qu'aucun utilisateur n'est dans le contexte
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // NOTE : Vous devez gérer ici si loadUserByUsername lève une exception (Utilisateur non trouvé)
            // Pour le moment, nous laissons la méthode la gérer.
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 3. Validation et Authentification
            if (jwtService.isTokenValid(token, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Utilisateur {} authentifié via JWT.", email);

            } else {
                logger.warn("Tentative d'accès avec token invalide ou expiré pour email: {}", email);
            }

        } else if (email == null && authHeader != null) {
            // Le token était présent mais invalide/expiré (loggué dans JwtService)
            logger.debug("Token JWT présent mais invalide/expiré. Contexte non établi.");
        }

        // 4. Continuation de la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}