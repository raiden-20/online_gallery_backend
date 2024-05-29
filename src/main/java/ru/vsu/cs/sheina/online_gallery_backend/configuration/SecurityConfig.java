package ru.vsu.cs.sheina.online_gallery_backend.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors((httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer
                        .configurationSource(corsConfigurationSource())))
                .exceptionHandling()
                .and()
                .authorizeHttpRequests()
//                    .requestMatchers("/change/**").hasRole(Role.USER.name())
                    .requestMatchers("/api/customers", "/api/artists", "/api/search/**", "/api/customer/{id}",
                            "/api/art/artId={artId}&currentId={currentId}", "/api/art/artist/artistId={artistId}&currentId={currentId}",
                            "/api/artist/artistId={artistId}&currentId={currentId}",
                            "/api/art/customer/{customerId}","/api/paintings", "/api/photos", "/api/sculptures",
                            "/api/auction/auctionId={auctionId}&currentId={currentId}",
                            "/api/auction/artist/{artistId}", "/api/auctions").permitAll()
                .and()
                .authorizeHttpRequests()
                .requestMatchers("swagger-ui/**", "swagger-ui**", "/v3/api-docs/**", "/v3/api-docs**")
                .permitAll()
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/api/notification/sse/{id}", "/api/auction/rates/userId={userId}&auctionId={auctionId}")
                .permitAll()
                .anyRequest()
                .authenticated();

        http
                .oauth2ResourceServer()
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter));

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
