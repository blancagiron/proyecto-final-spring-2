package es.blanca.application;

import es.blanca.domain.exceptions.EmailAlreadyExistsException;
import es.blanca.domain.exceptions.EntityNotFoundException;

import es.blanca.domain.model.Country;
import es.blanca.domain.model.User;
import es.blanca.domain.port.CountryRepository;
import es.blanca.domain.port.UserRepository;
import es.blanca.domain.port.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static es.blanca.application.config.ApplicationConstants.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final CountryRepository countryRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, CountryRepository countryRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.countryRepository = countryRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Optional<User> assignCountry(Long userId, String countryCode) {
		// find user
		log.info("Assigning country {} to user {}", countryCode, userId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId)));
		Country country = countryRepository.findById(countryCode)
				.orElseThrow(() -> new EntityNotFoundException(String.format(COUNTRY_NOT_FOUND_BY_CODE, countryCode)));

		user.setCountry(country);
		return Optional.of(userRepository.save(user));
	}

	@Override
	public List<User> findAll() {
		log.info("Finding all users");
		return userRepository.findAll();
	}

	@Override
	public Optional<User> findById(Long userId) {
		log.info("Finding user {}", userId);
		return Optional.of(userRepository.findById(userId).
				orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId))));
	}

	@Override
	public User create(User user) {
		log.info("Trying to create user with email {}", user.getEmail());
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new EmailAlreadyExistsException(String.format(EMAIL_ALREADY_EXISTS, user.getEmail()));
		}
		// encriptar contraseña
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setCreatedAt(LocalDateTime.now());
		user.setActive(true);
		log.info("User created successfully with id {}", user.getId());
		return userRepository.save(user);
	}

	@Override
	public void update(Long userId, User userWithNewData) {
		log.info("Trying to update user with id {}", userId);

		// 1. Carga el usuario existente de la base de datos.
		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId)));

		// 2. Actualiza solo los campos que vienen en el DTO (userWithNewData).
		//    NO toques la lista de pedidos.
		if (userWithNewData.getFullName() != null) {
			existingUser.setFullName(userWithNewData.getFullName());
		}

		if (userWithNewData.getEmail() != null) {
			// Opcional: Añadir validación de email duplicado aquí si es necesario
			existingUser.setEmail(userWithNewData.getEmail());
		}

		// La lógica de isActive debe ser manejada con un Boolean para evitar
		// la desactivación accidental, como se ve en tu fichero seguridad.md
		if (userWithNewData.isActive() != existingUser.isActive()) {
			existingUser.setActive(userWithNewData.isActive());
		}

		// 3. Guarda la entidad actualizada. Al hacer esto, la colección de pedidos
		//    original se mantiene intacta.
		userRepository.save(existingUser);
		log.info("User with id {} updated successfully", userId);
	}

	@Override
	public void delete(Long userId) {
		log.info("Trying to soft-delete user with id {}", userId);
		User userToDelete = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId)));
		userToDelete.setActive(false);
		userRepository.save(userToDelete);
		log.info("User {} marked as inactive", userId);
	}


}