package com.example.BookingRoom.confs;

import com.example.BookingRoom.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final PasswordEncoder passwordEncoder;
    private JwtFilter jwtFilter;

    public SecurityConfig(PasswordEncoder passwordEncoder, JwtFilter jwtFilter) {
        this.passwordEncoder = passwordEncoder;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return
                httpSecurity
                        .csrf(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests(
                                authorize ->
                                        authorize
                                                .requestMatchers("/**").permitAll()
//                                                .requestMatchers("/connexion").permitAll()
//                                                .requestMatchers("/reservations/**").permitAll()
//                                                .requestMatchers("/sse/**").permitAll()
//                                                .requestMatchers("/creeruser").permitAll()
//                                                .requestMatchers("/ecoles/**").permitAll()
//                                                .requestMatchers("/filieres/**").permitAll()
//                                                .requestMatchers("/infosuser").permitAll()
//                                                .requestMatchers("/infosconnexion").permitAll()
//                                                .requestMatchers("/reservations/pdf").permitAll()
//                                                .requestMatchers(GET, "/verifiertoken").permitAll()
//                                                .requestMatchers(GET, "/ecoles").permitAll()
//                                                .requestMatchers(GET, "/filieres/ecole").permitAll()
//                                                .requestMatchers(GET, "/filieres/id").permitAll()
//                                                .requestMatchers(GET, "/etudiants/filiere").permitAll()
//                                                .requestMatchers(GET, "/chambres/sexe").permitAll()
//                                                .requestMatchers("/chambres/importall").permitAll()
//                                                .requestMatchers("/**").hasAuthority("ADMIN")
                                                .anyRequest().authenticated()
                        ).sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                        .build();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new CustomUserDetailsService();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}