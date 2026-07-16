package com.turf.booking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF for simple REST API testing (re-enable in production with tokens)
            .csrf(csrf -> csrf.disable())
            
            // 2. Define the authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow anyone to view slots or make a booking (Guest access)
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/bookings/**").permitAll()
                
                // RESTRICT deletion strictly to users with the ADMIN role
                .requestMatchers(HttpMethod.DELETE, "/api/v1/bookings/**").hasRole("ADMIN")
                
                // Any other requests not explicitly mentioned must be authenticated
                .anyRequest().authenticated()
            )
            
            // 3. Use standard HTTP Basic authentication for the Admin login
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // 4. Create a temporary hardcoded Admin user to test your system
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            // {noop} tells Spring it's a plain-text password for local testing development
            .password("{noop}admin123") 
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
