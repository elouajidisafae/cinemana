package com.example.cinimana.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration // Indique que cette classe est une classe de configuration Spring
@ConfigurationProperties(prefix = "jwt")// Lie les propriétés de configuration avec le préfixe "jwt" aux champs de cette classe
@Data
public class JwtConfig {
    private String secret;// Clé secrète utilisée pour signer les JWT
    private long expiration;// Durée d'expiration des JWT en millisecondes
}
