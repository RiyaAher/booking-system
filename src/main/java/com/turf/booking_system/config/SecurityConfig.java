package com.turf.booking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
            // 1. Disable CSRF for simple testing / API calls
            .csrf(csrf -> csrf.disable())

            // 2. Allow frames so H2 Web Console works if accessed locally
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            
            // 3. Define authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow public access to Homepage, root path, static resources, and H2 console
                .requestMatchers("/", "/index", "/index.html", "/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico", "/h2-console/**").permitAll()

                // Allow ALL HTTP Methods (GET, POST, OPTIONS) for Chat & AI Endpoints
                .requestMatchers("/bookings/chat", "/bookings/chat/**", "/chat/**", "/api/chat/**").permitAll()
                
                // Allow public access to main dashboard paths & APIs
                .requestMatchers("/bookings", "/bookings/").permitAll()
                .requestMatchers("/api/v1/bookings/**").permitAll()
                
                // RESTRICT deletion strictly to users with the ADMIN role
                .requestMatchers(HttpMethod.DELETE, "/api/v1/bookings/**").hasRole("ADMIN")
                .requestMatchers("/bookings/delete/**").hasRole("ADMIN")
                
                // Any other requests (like Admin Dashboard pages) must be authenticated
                .anyRequest().authenticated()
            )
            
            // Form login settings
            .formLogin(form -> form
                .defaultSuccessUrl("/bookings", true) // Takes Admin straight to dashboard after login!
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/bookings") // Takes back to dashboard on logout
                .permitAll()
            );

        return http.build();
    }

    // Temporary hardcoded Admin user
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}admin123") 
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }
}