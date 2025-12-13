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
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Authentication manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // CORS Configuration Source
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:5174"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Activer CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF désactivé
                .csrf(csrf -> csrf.disable())

                // Gestion des erreurs JWT
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint))

                // Stateless session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Autorisations par rôle
                .authorizeHttpRequests(auth -> auth
                        // ✅ CORRECTION : Lister explicitement les chemins publics. /api/auth/reset-initial-password est exclu.
                        .requestMatchers(
                                "/api/auth/client/login",
                                "/api/auth/internal/login",
                                "/api/auth/register",
                                "/test-email",
                                "/", "/login", "/register", "/static/**", "/css/**", "/js/**"
                        ).permitAll()

                        // Protected by role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/commercial/**").hasRole("COMMERCIAL")
                        .requestMatchers("/caissier/**").hasRole("CAISSIER")
                        .requestMatchers("/client/**").hasRole("CLIENT")

                        // ✅ TOUS les autres chemins (dont /api/auth/reset-initial-password) nécessitent AUTHENTICATION.
                        .anyRequest().authenticated()
                )

                // Authentication provider + filtre JWT
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}