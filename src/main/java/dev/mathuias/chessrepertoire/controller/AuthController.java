package dev.mathuias.chessrepertoire.controller;

import dev.mathuias.chessrepertoire.auth.JwtService;
import dev.mathuias.chessrepertoire.auth.LoginRequest;
import dev.mathuias.chessrepertoire.auth.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.debug("Login attempt received for email={}", request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            log.debug("Credentials validated for email={}", request.email());
        } catch (AuthenticationException e) {
            log.warn("Login failed for email={}: {}", request.email(), e.getMessage());
            return ResponseEntity.status(401).build();
        }
        String token = jwtService.generateToken(request.email());
        log.info("JWT issued for email={}", request.email());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
