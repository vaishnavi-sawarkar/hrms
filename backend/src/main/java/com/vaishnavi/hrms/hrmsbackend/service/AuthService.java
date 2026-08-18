package com.vaishnavi.hrms.hrmsbackend.service;

import com.vaishnavi.hrms.hrmsbackend.dto.AuthResponse;
import com.vaishnavi.hrms.hrmsbackend.dto.LoginRequest;
import com.vaishnavi.hrms.hrmsbackend.dto.RegisterRequest;
import com.vaishnavi.hrms.hrmsbackend.model.Employee;
import com.vaishnavi.hrms.hrmsbackend.model.User;
import com.vaishnavi.hrms.hrmsbackend.repository.EmployeeRepository;
import com.vaishnavi.hrms.hrmsbackend.repository.UserRepository;
import com.vaishnavi.hrms.hrmsbackend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }

        User.UserBuilder userBuilder = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole());

        // Only EMPLOYEE self-registration requires linking to an existing employee record.
        // ADMIN/HR accounts are created separately and don't need this check.
        if ("EMPLOYEE".equalsIgnoreCase(request.getRole())) {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new RuntimeException("Email is required to register as an employee.");
            }

            Employee employee = employeeRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException(
                            "No employee record found for this email. Please contact HR to be added first."));

            userBuilder.employee(employee);
        }

        User user = userBuilder.build();
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}