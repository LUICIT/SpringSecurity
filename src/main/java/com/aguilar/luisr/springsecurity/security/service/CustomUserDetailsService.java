package com.aguilar.luisr.springsecurity.security.service;

import com.aguilar.luisr.springsecurity.domain.entity.User;
import com.aguilar.luisr.springsecurity.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // carga por email (username en este proyecto es el email)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Normaliza user_type y lo usa como rol (Roles se guardan sin el prefijo "ROLE_"; Spring lo añade)
        String role = (user.getUserType() == null || user.getUserType().isBlank())
                ? "USER"
                : user.getUserType().trim().toUpperCase();

        // usa la implementación de UserDetails de Spring Security
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(role) // produce authority "ROLE_{role}"
//                .authorities("USER") // ajustar según roles reales si existen
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

}
