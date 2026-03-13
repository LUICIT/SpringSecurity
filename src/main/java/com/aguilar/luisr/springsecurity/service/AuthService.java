package com.aguilar.luisr.springsecurity.service;

import com.aguilar.luisr.springsecurity.security.JwtUtil;
import com.aguilar.luisr.springsecurity.converter.UserConverter;
import com.aguilar.luisr.springsecurity.domain.entity.User;
import com.aguilar.luisr.springsecurity.domain.repository.UserRepository;
import com.aguilar.luisr.springsecurity.exceptions.EmailAlreadyUsedException;
import com.aguilar.luisr.springsecurity.web.model.LoginModel;
import com.aguilar.luisr.springsecurity.web.model.RegisterUserModel;
import com.aguilar.luisr.springsecurity.web.model.TokenModel;
import com.aguilar.luisr.springsecurity.web.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service class for managing authorizations.
 */
@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private static final int LOCK_HOURS = 1;

    private final UserConverter userConverter = new UserConverter();

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    // Self proxy para que @Transactional funcione (evita self-invocation)
    private final AuthService self;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       @Lazy AuthService self) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.self = self;
    }

    @Transactional
    public UserModel registerUser(RegisterUserModel registerUserModel) {
        if (userRepository.existsByEmail(registerUserModel.getEmail().toLowerCase())) {
            throw new EmailAlreadyUsedException();
        }

        String encryptedPassword = passwordEncoder.encode(registerUserModel.getPassword());
        User newUser = userConverter.toEntity(registerUserModel, encryptedPassword);

        userRepository.save(newUser);
        LOG.debug("Created Information for User: {}", newUser);

        return userConverter.toModel(newUser);
    }

    @Transactional
    public TokenModel login(LoginModel loginModel) {

        // 1) Normaliza email (evita duplicidad por mayúsculas/espacios)
        final String email = normalizeEmail(loginModel);

        // 2) Si existe usuario, valida lockout (sin filtrar info al cliente)
        User user = null;
        if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                if (isLocked(user)) {
                    throw new BadCredentialsException("Invalid credentials");
                }
                applyBackoffIfNeeded(user);
            }
        }

        try {
            // 3) Autentica con Spring Security (email/password)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, getPassword(loginModel))
            );

            // 4) Éxito: resetea contadores si el usuario existe
            if (user != null) {
                resetFailures(user);
                userRepository.save(user);
            }

            // 5) Genera JWT
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            assert principal != null;
            String token = jwtUtil.generateToken(principal);

            return new TokenModel(token, "Bearer");

        } catch (BadCredentialsException ex) {

            // 6) Fallo: incrementa contadores si el usuario existe
            // Nota: como aquí relanzamos una RuntimeException, la transacción actual haría rollback.
            // Por eso persistimos el intento fallido en una transacción nueva.
            if (user != null && user.getId() != null) {
                self.recordFailedLogin(user.getId());
            }

            // Respuesta uniforme
            throw ex;
        }
    }

    private void applyBackoffIfNeeded(User user) {
        int attempts = user.getFailedAttempts();

        // Empieza a ralentizar desde el 3er intento fallido
        if (attempts < 3) return;

        // 3->500ms, 4->1000ms, 5->2000ms, 6+->3000ms (cap)
        long delayMs = switch (attempts) {
            case 3 -> 500L;
            case 4 -> 1000L;
            case 5 -> 2000L;
            default -> 3000L;
        };

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(Long userId) {
        User managed = userRepository.findById(userId).orElse(null);
        if (managed == null) {
            return;
        }
        registerFailure(managed);
        userRepository.save(managed);
    }

    private boolean isLocked(User user) {
        LocalDateTime until = user.getLockedUntil();
        return until != null && until.isAfter(LocalDateTime.now());
    }

    private void resetFailures(User user) {
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastFailedAt(null);
    }

    private void registerFailure(User user) {
        int next = user.getFailedAttempts() + 1;
        user.setFailedAttempts(next);
        user.setLastFailedAt(LocalDateTime.now());

        if (next >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusHours(LOCK_HOURS));
        }
    }

    private String normalizeEmail(LoginModel loginModel) {
        String raw = (loginModel.getEmail() == null) ? null : loginModel.getEmail();
        return raw == null ? null : raw.trim().toLowerCase();
    }

    private String getPassword(LoginModel loginModel) {
        return loginModel.getPassword();
    }

}
