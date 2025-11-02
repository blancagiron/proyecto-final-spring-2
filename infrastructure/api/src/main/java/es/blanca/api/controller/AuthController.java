package es.blanca.api.controller;

import es.blanca.api.dto.auth.AuthResponse;
import es.blanca.api.dto.auth.LoginRequest;
import es.blanca.api.dto.auth.RegisterRequest;
import es.blanca.api.security.JwtTokenProvider;
import es.blanca.domain.exceptions.EmailAlreadyExistsException;
import es.blanca.domain.exceptions.InvalidPasswordException;
import es.blanca.domain.model.Role;
import es.blanca.domain.model.User;
import es.blanca.domain.port.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider tokenProvider;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
		log.info("Login attempt for email: {}", loginRequest.getEmail());

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequest.getEmail(),
						loginRequest.getPassword()
				)
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.generateToken(authentication);

		User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();

		log.info("User {} logged in successfully", loginRequest.getEmail());
		return ResponseEntity.ok(new AuthResponse(jwt, user.getEmail(), user.getRole().name()));
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
		log.info("Registration attempt for email: {}", registerRequest.getEmail());

		// Validar que el email no exista
		if (userRepository.existsByEmail(registerRequest.getEmail())) {
			throw new EmailAlreadyExistsException("Email already exists: " + registerRequest.getEmail());
		}

		// Validar contraseña (la anotación @Pattern ya lo hace, pero por si acaso)
		validatePassword(registerRequest.getPassword());

		// Crear usuario
		User user = new User();
		user.setFullName(registerRequest.getFullName());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setRole(Role.ROLE_USER); // Por defecto USER
		user.setCreatedAt(LocalDateTime.now());
		user.setActive(true);

		userRepository.save(user);
		log.info("User registered successfully: {}", user.getEmail());

		// Auto-login después del registro
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						registerRequest.getEmail(),
						registerRequest.getPassword()
				)
		);

		String jwt = tokenProvider.generateToken(authentication);
		return new ResponseEntity<>(
				new AuthResponse(jwt, user.getEmail(), user.getRole().name()),
				HttpStatus.CREATED
		);
	}

	private void validatePassword(String password) {
		if (password.length() < 8) {
			throw new InvalidPasswordException("Password must be at least 8 characters long");
		}
		if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
			throw new InvalidPasswordException(
					"Password must contain at least one uppercase letter, one lowercase letter, and one number");
		}
	}
}