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
            // 1. Disable CSRF for simple testing
            .csrf(csrf -> csrf.disable())
            
            // 2. Define the authorization rules
            .authorizeHttpRequests(auth -> auth
                // FIXED: Explicitly allow anyone to access the main HTML dashboard path!
                .requestMatchers("/bookings").permitAll()
                
                // Allow anyone to use guest REST APIs
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/bookings/**").permitAll()
                
                // RESTRICT deletion strictly to users with the ADMIN role
                .requestMatchers(HttpMethod.DELETE, "/api/v1/bookings/**").hasRole("ADMIN")
                .requestMatchers("/bookings/delete/**").hasRole("ADMIN")
                
                // Any other requests must be authenticated
                .anyRequest().authenticated()
            )
            
            // FIXED: Swapped .httpBasic() for .formLogin() so the browser shows a clean interface
            .formLogin(Customizer.withDefaults());

        return http.build();
    }

    // 4. Temporary hardcoded Admin user
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