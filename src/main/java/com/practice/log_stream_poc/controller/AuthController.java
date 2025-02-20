package com.practice.log_stream_poc.controller;

import com.practice.log_stream_poc.model.dto.AuthRequestDto;
import com.practice.log_stream_poc.model.dto.AuthResponseDto;
import com.practice.log_stream_poc.service.AuthenticationService;
import com.practice.log_stream_poc.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserDetailsService userDetailsService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequestDto request) {
        if (userDetailsService.loadUserByUsername(request.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        authenticationService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto request) {
        log.debug("New login request");
        var authenticatedUser = authenticationService.login(request);
        var authResponse = new AuthResponseDto(jwtService.generateToken(authenticatedUser.getUsername()));
        return ResponseEntity.ok(authResponse);
    }
}
