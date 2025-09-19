package com.annuaire.khalifa.annuaire;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class KeycloakSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // ✅ Endpoints publics pour les tests et l'accueil
                        .requestMatchers("/", "/accueil", "/login", "/static/**", "/assets/**").permitAll()

                        // ✅ API endpoints publics
                        .requestMatchers(HttpMethod.GET, "/api/employes/count").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employes/combined").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employes/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employes/test-mock/**").permitAll()

                        // ✅ Login endpoint public
                        .requestMatchers(HttpMethod.POST, "/api/employes/login").permitAll()

                        // ✅ Endpoints protégés (nécessitent un token JWT)
                        .requestMatchers(HttpMethod.POST, "/api/employes").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/employes/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/employes/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/employes/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/employes/logout").authenticated()

                        // ✅ Autres endpoints GET protégés
                        .requestMatchers(HttpMethod.GET, "/api/employes/**").authenticated()

                        // Le reste sécurisé par défaut
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter()))
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("🚨 Erreur auth sur: " + request.getRequestURI());

                            // ✅ Endpoints publics : pas d'erreur 401
                            String uri = request.getRequestURI();
                            if (uri.equals("/") || uri.equals("/accueil") || uri.startsWith("/static") ||
                                    uri.startsWith("/assets") || uri.equals("/api/employes/login") ||
                                    uri.equals("/api/employes/count") || uri.equals("/api/employes/combined") ||
                                    uri.equals("/api/employes/search") || uri.startsWith("/api/employes/test-mock")) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                return;
                            }

                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                        })
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = "https://refonte.seneau.sn/realms/auth2-dev/protocol/openid-connect/certs";

                // ✅ Configuration pour éviter les erreurs de cache
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Origine spécifique pour la prod, wildcard pour dev
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "https://refonte.seneau.sn"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}