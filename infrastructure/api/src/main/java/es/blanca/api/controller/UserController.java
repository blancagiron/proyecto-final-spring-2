package es.blanca.api.controller;

import es.blanca.api.dto.input.UserCreateInputDto;
import es.blanca.api.dto.input.UserUpdateDto;
import es.blanca.api.dto.output.UserOutputDto;
import es.blanca.api.mapper.UserApiMapper;
import es.blanca.domain.exceptions.ForbiddenOperationException;
import es.blanca.domain.model.Role;
import es.blanca.domain.model.User;
import es.blanca.domain.port.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final UserApiMapper userApiMapper;
	private final PasswordEncoder passwordEncoder;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserOutputDto> createUser(@Valid @RequestBody UserCreateInputDto dto) {
		log.info("Attempting to create user with email: {}", dto.getEmail());
		User user = userApiMapper.toDomain(dto);
		user.setRole(Role.ROLE_USER);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		User createdUser = userService.create(user);
		return new ResponseEntity<>(userApiMapper.toOutputDto(createdUser), HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserOutputDto>> getAllUsers() {
		log.info("Fetching all users");
		return ResponseEntity.ok(userService.findAll().stream()
				.map(userApiMapper::toOutputDto)
				.collect(Collectors.toList()));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserOutputDto> getUserById(@PathVariable Long id) {
		log.info("Fetching user with id: {}", id);
		User user = userService.findById(id).orElseThrow();
		return ResponseEntity.ok(userApiMapper.toOutputDto(user));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserOutputDto> updateUser(
			@PathVariable Long id,
			@Valid @RequestBody UserUpdateDto dto) {

		log.info("Attempting to update user with id: {}", id);

		// ✅ SOLUCIÓN: Obtener usuario existente
		User existingUser = userService.findById(id).orElseThrow();

		// Actualizar solo campos del DTO
		if (dto.getFullName() != null) {
			existingUser.setFullName(dto.getFullName());
		}

		if (dto.getEmail() != null) {
			existingUser.setEmail(dto.getEmail());
		}

		if (dto.getIsActive() != null) {
			existingUser.setActive(dto.getIsActive());
		}

		// ⚠️ NO tocar existingUser.setOrders(...)

		userService.update(id, existingUser);
		User updatedUser = userService.findById(id).orElseThrow();

		return ResponseEntity.ok(userApiMapper.toOutputDto(updatedUser));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable Long id) {
		log.info("Attempting to soft-delete user with id: {}", id);
		userService.delete(id);
	}

	@PatchMapping("/{id}/country")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<UserOutputDto> assignCountry(
			@PathVariable Long id,
			@RequestParam String countryCode) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUserEmail = authentication.getName();

		// Si es USER, solo puede modificar su propio país
		if (authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
			User currentUser = userService.findAll().stream()
					.filter(u -> u.getEmail().equals(currentUserEmail))
					.findFirst()
					.orElseThrow();

			if (!currentUser.getId().equals(id)) {
				log.warn("User {} attempted to modify country for another user", currentUserEmail);
				throw new ForbiddenOperationException("You can only modify your own country");
			}
		}

		log.info("Assigning country {} to user {}", countryCode, id);
		User updatedUser = userService.assignCountry(id, countryCode).orElseThrow();
		return ResponseEntity.ok(userApiMapper.toOutputDto(updatedUser));
	}
}