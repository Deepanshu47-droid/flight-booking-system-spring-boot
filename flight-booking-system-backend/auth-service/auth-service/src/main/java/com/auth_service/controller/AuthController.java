package com.auth_service.controller;

import com.auth_service.dto.LoginRequest;
import com.auth_service.dto.LoginResponse;
import com.auth_service.dto.ResetPasswordRequest;
import com.auth_service.model.AppUser;
import com.auth_service.model.Role;
import com.auth_service.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/{role}")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, @PathVariable String role) {
        return ResponseEntity.ok(authService.login(request, role));
    }

    @PostMapping("/register/{role}")
    public ResponseEntity<String> register(@Valid @RequestBody AppUser appUser, @PathVariable String role) {
        return authService.register(appUser, role);
    }

    @PostMapping("/verify-otp/{role}")
    public ResponseEntity<String> verifyOtpAndRegister(@Valid @RequestBody AppUser appUser, @RequestParam String otp, @PathVariable String role) {
        return authService.verifyOtpAndRegister(appUser, otp, role);
    }

    @PostMapping("/reset-password/{role}")
    public ResponseEntity<String> forgotPassword(@RequestParam String email, @PathVariable String role) {
        return authService.forgotPassword(email, role);
    }

    @PutMapping("/reset/verify-otp/{role}")
    public ResponseEntity<String> verifyResetPassword(@Valid @RequestBody ResetPasswordRequest request, @PathVariable String role) {
        return authService.verifyResetPasswordOtp(request, role);
    }

    @DeleteMapping("/delete/{role}")
    public ResponseEntity<String> deleteUser(@RequestBody LoginRequest loginRequest, @PathVariable String role) {
        authService.deleteRole(loginRequest, role);
        return ResponseEntity.ok(role + " deleted successfully.");
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        authService.validateToken(token);
        return "Token is valid";
    }
}
