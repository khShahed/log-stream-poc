package com.practice.log_stream_poc.service;

import com.practice.log_stream_poc.model.dto.AuthRequestDto;
import com.practice.log_stream_poc.model.entity.User;
import com.practice.log_stream_poc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public User register(AuthRequestDto input) {
        User user = User.builder()
            .username(input.getUsername())
            .password(passwordEncoder.encode(input.getPassword()))
            .build();

        return userRepository.save(user);
    }

    public User login(AuthRequestDto input) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                input.getUsername(),
                input.getPassword()
            )
        );

        return userRepository.findByUsername(input.getUsername())
            .orElseThrow();
    }

}
