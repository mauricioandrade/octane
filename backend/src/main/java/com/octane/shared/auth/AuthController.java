package com.octane.shared.auth;

import com.octane.audit.usecase.AuditService;
import com.octane.user.domain.User;
import com.octane.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final AuditService auditService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          AuthenticatedUserService authenticatedUserService,
                          AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public AuthUserResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
            );
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            var stationIds = userRepository.findStationIdsByUserId(user.getId());
            auditService.log("LOGIN", "User", user.getId(), null);
            return AuthUserResponse.from(user, stationIds);
        } catch (BadCredentialsException ex) {
            throw new com.octane.shared.exception.BusinessException("Usuário ou senha inválidos");
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        auditService.log("LOGOUT", "User", null, null);
        session.invalidate();
        SecurityContextHolder.clearContext();
    }

    @GetMapping("/me")
    public AuthUserResponse me() {
        var currentUser = authenticatedUserService.getCurrentUser();
        var user = userRepository.findByUsername(currentUser.username()).orElseThrow();
        return AuthUserResponse.from(user, currentUser.stationIds());
    }
}
