package com.lifeline.emergency.service;

import com.lifeline.emergency.dto.*;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.entity.User;
import com.lifeline.emergency.enums.Role;
import com.lifeline.emergency.repository.HospitalRepository;
import com.lifeline.emergency.repository.UserRepository;
import com.lifeline.emergency.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       HospitalRepository hospitalRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with email " + request.getEmail() + " already exists.");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                request.getAge(),
                request.getGender(),
                request.getKnownConditions(),
                Role.ROLE_USER
        );

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name(), savedUser.getId(), savedUser.getName());

        return new AuthResponse(token, savedUser.getRole().name(), savedUser.getId(), savedUser.getName(), savedUser.getEmail(), "User registration successful");
    }

    public AuthResponse loginUser(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (user.isBlocked()) {
            throw new IllegalStateException("Your user account has been suspended by the administrator. Reason: " +
                    (user.getBlockedReason() != null ? user.getBlockedReason() : "Account under review."));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId(), user.getName());
        return new AuthResponse(token, user.getRole().name(), user.getId(), user.getName(), user.getEmail(), "Login successful");
    }

    public AuthResponse registerHospital(HospitalRegisterRequest request) {
        if (hospitalRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Hospital with email " + request.getEmail() + " already exists.");
        }

        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setEmail(request.getEmail());
        hospital.setPassword(passwordEncoder.encode(request.getPassword()));
        hospital.setContactNumber(request.getContactNumber());
        hospital.setAddress(request.getAddress());
        hospital.setLatitude(request.getLatitude() != null ? request.getLatitude() : 22.5726);
        hospital.setLongitude(request.getLongitude() != null ? request.getLongitude() : 88.3639);
        hospital.setLicenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "WB-MED-" + System.currentTimeMillis() % 100000);
        hospital.setDirectorName(request.getDirectorName() != null ? request.getDirectorName() : "Chief Medical Officer");

        // Hospital registration requires Super Admin verification before login is permitted
        hospital.setActive(true);
        hospital.setVerified(false);
        hospital.setOpen(false);
        hospital.setHasEmergency(true);
        hospital.setClosedPermanently(false);

        Hospital saved = hospitalRepository.save(hospital);

        // No login token returned - pending Super Admin inspection
        return new AuthResponse(null, Role.ROLE_HOSPITAL_ADMIN.name(), saved.getId(), saved.getName(), saved.getEmail(),
                "Hospital registered successfully. Your registration has been submitted for Super Admin verification. You will be able to log in once your medical license is verified.");
    }

    public AuthResponse loginHospital(AuthRequest request) {
        Hospital hospital = hospitalRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid hospital credentials."));

        if (!passwordEncoder.matches(request.getPassword(), hospital.getPassword())) {
            throw new IllegalArgumentException("Invalid hospital credentials.");
        }

        if (hospital.isClosedPermanently()) {
            throw new IllegalStateException("This hospital has been marked as permanently closed by the Health Authority.");
        }

        if (!hospital.isVerified()) {
            throw new IllegalStateException("Hospital account is pending Super Admin verification. Approval is required before login is granted.");
        }

        if (!hospital.isActive()) {
            throw new IllegalStateException("Hospital account has been suspended by the platform administrator.");
        }

        String token = jwtUtil.generateToken(hospital.getEmail(), Role.ROLE_HOSPITAL_ADMIN.name(), hospital.getId(), hospital.getName());
        return new AuthResponse(token, Role.ROLE_HOSPITAL_ADMIN.name(), hospital.getId(), hospital.getName(), hospital.getEmail(), "Hospital login successful");
    }
}

