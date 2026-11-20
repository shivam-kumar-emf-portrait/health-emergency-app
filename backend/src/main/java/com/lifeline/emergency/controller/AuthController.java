package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.*;
import com.lifeline.emergency.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.loginUser(request));
    }

    @PostMapping("/hospital/register")
    public ResponseEntity<AuthResponse> registerHospital(@RequestBody HospitalRegisterRequest request) {
        return ResponseEntity.ok(authService.registerHospital(request));
    }

    @PostMapping("/hospital/login")
    public ResponseEntity<AuthResponse> loginHospital(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.loginHospital(request));
    }
}

