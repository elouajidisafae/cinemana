// src/main/java/com/example/cinimana/config/SecurityConfig.java
package com.example.cinimana.config;

import com.example.cinimana.security.jwt.JwtAuthenticationEntryPoint;
import com.example.cinimana.security.jwt.JwtAuthenticationFilter;
import com.example.cinimana.security.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtEntryPoint;
    private final CustomUserDetailsService userDetailsService;

    // Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication provider
    @Bean //Configurer l'AuthenticationProvider pour utiliser le CustomUserDetailsService et le PasswordEncoder
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    //CustomUserDetailsService est la classe qui permet à Spring Security de savoir comment récupérer un utilisateur depuis la base de données.

    // Authentication manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS Configuration Source
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(         // Ajouter les origines autorisées
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:5174"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // Autoriser tous les en-têtes
        configuration.setAllowCredentials(true); // Autoriser les cookies et les informations d'authentification
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));// Exposer les en-têtes nécessaires
        configuration.setMaxAge(3600L); // Durée de vie de la configuration CORS en secondes

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Appliquer cette configuration à toutes les URL
        return source;
    }

    // Security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Activer CORS  qui permet d’autoriser explicitement le Frontend React à communiquer avec le Backend Spring Boot.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF désactivé
                .csrf(csrf -> csrf.disable())   //désactivation de la protection CSRF car nous utilisons des tokens JWT

                // Gestion des erreurs JWT
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint))

                // Stateless session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //ici on configure Spring Security pour qu'il n'utilise pas de sessions HTTP pour stocker l'état d'authentification.
                // Autorisations par rôle
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/client/login",
                                "/api/auth/internal/login",
                                "/api/auth/register",
                                "/api/public/**",
                                "/api/offres/public",
                                "/api/client/reservations/*/confirm-presence", // Autoriser la confirmation sans login
                                "/api/client/reservations/*/confirm",          // Autoriser le lien direct backend si utilisé
                                "/test-email",
                                "/", "/login", "/register", "/static/**", "/css/**", "/js/**", "/uploads/**")
                        .permitAll()

                        // Protected by role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/offres/**").hasRole("ADMIN")
                        .requestMatchers("/api/commercial/**").hasRole("COMMERCIAL")
                        .requestMatchers("/api/caissier/**").hasRole("CAISSIER")
                        .requestMatchers("/api/client/**").hasRole("CLIENT")

                        // Toute autre requête nécessite une authentification
                        .anyRequest().authenticated())

                // Authentication provider + filtre JWT
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}