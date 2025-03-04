package com.byteforge.auth.service;
import com.byteforge.auth.dto.AuthRequest;
import com.byteforge.auth.dto.AuthResponse;
import com.byteforge.auth.dto.RegisterRequest;
import com.byteforge.auth.model.User;
import com.byteforge.auth.repository.UserRepository;
import com.byteforge.auth.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.byteforge.exception.ResourceAlreadyExistsException;

import java.util.HashSet;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsernameOrEmail(),
                        authRequest.getPassword()
                )
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsernameOrEmail());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Find the user to get their actual username and email
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());

        // Set default role if none provided
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            HashSet<String> roles = new HashSet<>();
            roles.add("USER");
            user.setRoles(roles);
        } else {
            user.setRoles(registerRequest.getRoles());
        }

        userRepository.save(user);

        // Generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }
}
