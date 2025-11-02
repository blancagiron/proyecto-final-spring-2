package es.blanca.application;

import es.blanca.domain.exceptions.EmailAlreadyExistsException;
import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Country;
import es.blanca.domain.model.Role;
import es.blanca.domain.model.User;
import es.blanca.domain.port.CountryRepository;
import es.blanca.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private CountryRepository countryRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	private User user;
	private Country country;

	@BeforeEach
	void setUp() {
		country = new Country("ES", "Spain");

		user = new User();
		user.setId(1L);
		user.setFullName("Test User");
		user.setEmail("test@example.com");
		user.setPassword("plainPassword");
		user.setRole(Role.ROLE_USER);
		user.setActive(true);
		user.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void findAll_shouldReturnAllUsers() {
		// Arrange
		User user2 = new User();
		user2.setId(2L);
		user2.setEmail("user2@example.com");

		when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

		// Act
		List<User> result = userService.findAll();

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(userRepository, times(1)).findAll();
	}

	@Test
	void findById_shouldReturnUser_whenExists() {
		// Arrange
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		// Act
		Optional<User> result = userService.findById(1L);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(1L, result.get().getId());
		verify(userRepository, times(1)).findById(1L);
	}

	@Test
	void findById_shouldThrowException_whenNotExists() {
		// Arrange
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			userService.findById(999L);
		});
	}

	@Test
	void create_shouldCreateUser_whenEmailIsUnique() {
		// Arrange
		when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
		when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
		when(userRepository.save(any(User.class))).thenReturn(user);

		// Act
		User result = userService.create(user);

		// Assert
		assertNotNull(result);
		verify(userRepository, times(1)).existsByEmail(user.getEmail());
		verify(passwordEncoder, times(1)).encode("plainPassword");
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void create_shouldThrowException_whenEmailAlreadyExists() {
		// Arrange
		when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

		// Act & Assert
		assertThrows(EmailAlreadyExistsException.class, () -> {
			userService.create(user);
		});
		verify(userRepository, never()).save(any());
	}

	@Test
	void update_shouldUpdateUser_whenExists() {
		// Arrange
		User existingUser = new User();
		existingUser.setId(1L);
		existingUser.setFullName("Old Name");
		existingUser.setEmail("old@example.com");
		existingUser.setActive(true);

		User updateData = new User();
		updateData.setFullName("New Name");
		updateData.setEmail("new@example.com");
		updateData.setActive(false);

		when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(existingUser);

		// Act
		userService.update(1L, updateData);

		// Assert
		verify(userRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void update_shouldUpdateOnlyFullName_whenOnlyFullNameProvided() {
		// Arrange
		User existingUser = new User();
		existingUser.setId(1L);
		existingUser.setFullName("Old Name");
		existingUser.setEmail("test@example.com");

		User updateData = new User();
		updateData.setFullName("New Name");

		when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(existingUser);

		// Act
		userService.update(1L, updateData);

		// Assert
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void update_shouldThrowException_whenUserNotFound() {
		// Arrange
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			userService.update(999L, user);
		});
		verify(userRepository, never()).save(any());
	}

	@Test
	void delete_shouldSetUserInactive() {
		// Arrange
		User existingUser = new User();
		existingUser.setId(1L);
		existingUser.setActive(true);

		when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(existingUser);

		// Act
		userService.delete(1L);

		// Assert
		verify(userRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void delete_shouldThrowException_whenUserNotFound() {
		// Arrange
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			userService.delete(999L);
		});
		verify(userRepository, never()).save(any());
	}

	@Test
	void assignCountry_shouldAssignCountry_whenBothExist() {
		// Arrange
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(countryRepository.findById("ES")).thenReturn(Optional.of(country));
		when(userRepository.save(any(User.class))).thenReturn(user);

		// Act
		Optional<User> result = userService.assignCountry(1L, "ES");

		// Assert
		assertTrue(result.isPresent());
		verify(userRepository, times(1)).findById(1L);
		verify(countryRepository, times(1)).findById("ES");
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void assignCountry_shouldThrowException_whenUserNotFound() {
		// Arrange
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			userService.assignCountry(999L, "ES");
		});
		verify(countryRepository, never()).findById(any());
	}

	@Test
	void assignCountry_shouldThrowException_whenCountryNotFound() {
		// Arrange
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(countryRepository.findById("XX")).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			userService.assignCountry(1L, "XX");
		});
		verify(userRepository, never()).save(any());
	}
}