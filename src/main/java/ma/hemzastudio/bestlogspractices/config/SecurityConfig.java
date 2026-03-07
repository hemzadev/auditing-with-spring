package ma.hemzastudio.bestlogspractices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
/**
 * HTTP Basic security — suitable for local testing.
 *-
 * Two in-memory users:
 *   user  / password  → ROLE_USER  (CRUD on /api/users/**)
 *   admin / password  → ROLE_ADMIN (everything including /api/admin/**)
 *-
 * To swap to JWT later: replace httpBasic() with a JWT filter
 * and remove InMemoryUserDetailsManager. The rest stays identical.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controllers
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/actuator/health").permitAll()
                        // Admin-only routes
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Standard user routes
                        .requestMatchers(HttpMethod.POST,   "/api/users").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH,  "/api/users/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET,    "/api/users/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * In-memory users for testing only.
     * Replace with a real UserDetailsService backed by DB in production.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.builder()
                        .username("user")
                        .password(encoder.encode("password"))
                        .roles("USER")
                        .build(),
                User.builder()
                        .username("admin")
                        .password(encoder.encode("password"))
                        .roles("USER", "ADMIN")
                        .build()
        );
    }
}