package com.auth_service.services;

import com.auth_service.config.SecurityConfig;
import com.auth_service.dto.LoginRequest;
import com.auth_service.dto.LoginResponse;
import com.auth_service.dto.ResetPasswordRequest;
import com.auth_service.exceptions.MailServiceException;
import com.auth_service.external.MailClient;
import com.auth_service.model.AppUser;
import com.auth_service.model.Role;
import com.auth_service.repositories.AppUserRepository;
import com.auth_service.security.JwtHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager manager;
    private final AppUserRepository appUserRepository;
    private final JwtHelper jwtHelper;
    private final SecurityConfig securityConfig;
    private final MailClient mailClient;

    public AuthService(AuthenticationManager manager, AppUserRepository appUserRepository, JwtHelper jwtHelper, SecurityConfig securityConfig, MailClient mailClient) {
        this.manager = manager;
        this.appUserRepository = appUserRepository;
        this.jwtHelper = jwtHelper;
        this.securityConfig = securityConfig;
        this.mailClient = mailClient;
    }

    public void doAuthenticate(String username, String password) {
        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
            manager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public LoginResponse login(LoginRequest request, String role) {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        doAuthenticate(request.getUsername(), request.getPassword());

        AppUser appUser = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(role + " not found with this username"));

        if (appUser.getRole() != userRole) {
            throw new BadCredentialsException(role + " not found");
        }

        String token = jwtHelper.generateToken(appUser.getUsername(), userRole);
        return LoginResponse.builder()
                .token(token)
                .username(appUser.getUsername())
                .build();
    }

    public ResponseEntity<String> register(AppUser appUser, String role) {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        if (appUserRepository.existsByEmailAndRole(appUser.getEmail(), userRole)) {
            throw new IllegalArgumentException("Email already registered!");
        }

        if (appUserRepository.existsByUsernameAndRole(appUser.getUsername(), userRole)) {
            throw new IllegalArgumentException("Username already registered!");
        }

        try {
            mailClient.generateAndSendOtp(appUser.getEmail());
        } catch (feign.FeignException.Unauthorized e) {
            throw new MailServiceException("Mail authentication failed. Please check mail service credentials.");
        } catch (feign.FeignException e) {
            throw new MailServiceException("Failed to send OTP. Mail service error: " + e.getMessage());
        }
        return ResponseEntity.ok("OTP sent successfully. Please verify OTP to complete registration.");
    }

    public ResponseEntity<String> verifyOtpAndRegister(AppUser appUser, String otp, String role) {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        if (!mailClient.verifyOtp(appUser.getEmail(), otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP!");
        }

        appUser.setPassword(securityConfig.passwordEncoder().encode(appUser.getPassword()));
        appUser.setRole(userRole);
        appUserRepository.save(appUser);

        return ResponseEntity.ok(role + " registered successfully");
    }

    public ResponseEntity<String> forgotPassword(String email, String role) {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        AppUser appUser = appUserRepository.findByEmailAndRole(email, userRole)
                .orElseThrow(() -> new RuntimeException(role + " with this email not found!"));

        try {
            mailClient.generateAndSendOtp(appUser.getEmail());
        } catch (feign.FeignException.Unauthorized e) {
            throw new MailServiceException("Mail authentication failed. Please check mail service credentials.");
        } catch (feign.FeignException e) {
            throw new MailServiceException("Failed to send OTP. Mail service error: " + e.getMessage());
        }
        return ResponseEntity.ok("OTP sent to your email. Please verify to reset your password.");
    }

    public ResponseEntity<String> verifyResetPasswordOtp(ResetPasswordRequest request, String role) {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        if (!mailClient.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired OTP!");
        }

        AppUser appUser = appUserRepository.findByEmailAndRole(request.getEmail(), userRole)
                .orElseThrow(() -> new RuntimeException(role + " with this email not found!"));

        appUser.setPassword(securityConfig.passwordEncoder().encode(request.getNewPassword()));
        appUserRepository.save(appUser);

        return ResponseEntity.ok("Password reset successfully.");
    }

    public void deleteRole(LoginRequest request, String role) {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        doAuthenticate(request.getUsername(), request.getPassword());

        appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(role + " not found with this username"));

        appUserRepository.deleteByUsername(request.getUsername());
    }

    public void validateToken(String token) {
        jwtHelper.validateToken(token);
    }
}
