package es.blanca.jpa.adapter;

import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Role;
import es.blanca.domain.model.User;
import es.blanca.jpa.entity.UserEntity;
import es.blanca.jpa.mapper.UserPersistenceMapper;
import es.blanca.jpa.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

	@Mock
	private UserJpaRepository userJpaRepository;

	@Mock
	private UserPersistenceMapper userPersistenceMapper;

	@InjectMocks
	private UserRepositoryAdapter userRepositoryAdapter;

	private User user;
	private UserEntity userEntity;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setFullName("Test User");
		user.setPassword("hashedPassword");
		user.setRole(Role.ROLE_USER);
		user.setActive(true);
		user.setCreatedAt(LocalDateTime.now());

		userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setEmail("test@example.com");
		userEntity.setFullName("Test User");
		userEntity.setPassword("hashedPassword");
		userEntity.setRole(Role.ROLE_USER);
		userEntity.setActive(true);
		userEntity.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void save_shouldSaveNewUser_whenUserHasNoId() {
		// Arrange
		User newUser = new User();
		newUser.setEmail("new@example.com");
		newUser.setFullName("New User");

		UserEntity newEntity = new UserEntity();
		newEntity.setEmail("new@example.com");

		when(userPersistenceMapper.toEntity(any(User.class))).thenReturn(newEntity);
		when(userJpaRepository.save(any(UserEntity.class))).thenReturn(newEntity);
		when(userPersistenceMapper.toDomain(any(UserEntity.class))).thenReturn(newUser);

		// Act
		User savedUser = userRepositoryAdapter.save(newUser);

		// Assert
		assertNotNull(savedUser);
		verify(userJpaRepository, times(1)).save(newEntity);
		verify(userJpaRepository, never()).findById(any());
	}

	@Test
	void save_shouldUpdateExistingUser_whenUserHasId() {
		// Arrange
		when(userPersistenceMapper.toEntity(any(User.class))).thenReturn(userEntity);
		when(userJpaRepository.findById(1L)).thenReturn(Optional.of(userEntity));
		when(userJpaRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		when(userPersistenceMapper.toDomain(any(UserEntity.class))).thenReturn(user);

		// Act
		User savedUser = userRepositoryAdapter.save(user);

		// Assert
		assertNotNull(savedUser);
		assertEquals(1L, savedUser.getId());
		verify(userJpaRepository, times(1)).findById(1L);
		verify(userJpaRepository, times(1)).save(userEntity);
	}

	@Test
	void save_shouldThrowException_whenUpdatingNonExistentUser() {
		// Arrange
		when(userPersistenceMapper.toEntity(any(User.class))).thenReturn(userEntity);
		when(userJpaRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			userRepositoryAdapter.save(user);
		});

		verify(userJpaRepository, times(1)).findById(1L);
		verify(userJpaRepository, never()).save(any());
	}

	@Test
	void findById_shouldReturnUser_whenIdExists() {
		// Arrange
		when(userJpaRepository.findById(1L)).thenReturn(Optional.of(userEntity));
		when(userPersistenceMapper.toDomain(any(UserEntity.class))).thenReturn(user);

		// Act
		Optional<User> foundUser = userRepositoryAdapter.findById(1L);

		// Assert
		assertTrue(foundUser.isPresent());
		assertEquals(1L, foundUser.get().getId());
		assertEquals("test@example.com", foundUser.get().getEmail());
		verify(userJpaRepository, times(1)).findById(1L);
	}

	@Test
	void findById_shouldReturnEmpty_whenIdDoesNotExist() {
		// Arrange
		when(userJpaRepository.findById(999L)).thenReturn(Optional.empty());

		// Act
		Optional<User> foundUser = userRepositoryAdapter.findById(999L);

		// Assert
		assertFalse(foundUser.isPresent());
		verify(userJpaRepository, times(1)).findById(999L);
	}

	@Test
	void findByEmail_shouldReturnUser_whenEmailExists() {
		// Arrange
		when(userJpaRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
		when(userPersistenceMapper.toDomain(any(UserEntity.class))).thenReturn(user);

		// Act
		Optional<User> foundUser = userRepositoryAdapter.findByEmail("test@example.com");

		// Assert
		assertTrue(foundUser.isPresent());
		assertEquals("test@example.com", foundUser.get().getEmail());
		verify(userJpaRepository, times(1)).findByEmail("test@example.com");
	}

	@Test
	void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
		// Arrange
		when(userJpaRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

		// Act
		Optional<User> foundUser = userRepositoryAdapter.findByEmail("nonexistent@example.com");

		// Assert
		assertFalse(foundUser.isPresent());
		verify(userJpaRepository, times(1)).findByEmail("nonexistent@example.com");
	}

	@Test
	void findAll_shouldReturnAllUsers() {
		// Arrange
		UserEntity user2Entity = new UserEntity();
		user2Entity.setId(2L);
		user2Entity.setEmail("user2@example.com");

		User user2 = new User();
		user2.setId(2L);
		user2.setEmail("user2@example.com");

		when(userJpaRepository.findAll()).thenReturn(Arrays.asList(userEntity, user2Entity));
		when(userPersistenceMapper.toDomain(userEntity)).thenReturn(user);
		when(userPersistenceMapper.toDomain(user2Entity)).thenReturn(user2);

		// Act
		List<User> users = userRepositoryAdapter.findAll();

		// Assert
		assertNotNull(users);
		assertEquals(2, users.size());
		verify(userJpaRepository, times(1)).findAll();
	}

	@Test
	void findAll_shouldReturnEmptyList_whenNoUsers() {
		// Arrange
		when(userJpaRepository.findAll()).thenReturn(Collections.emptyList());

		// Act
		List<User> users = userRepositoryAdapter.findAll();

		// Assert
		assertNotNull(users);
		assertTrue(users.isEmpty());
		verify(userJpaRepository, times(1)).findAll();
	}

	@Test
	void deleteById_shouldCallJpaRepository() {
		// Arrange
		doNothing().when(userJpaRepository).deleteById(1L);

		// Act
		userRepositoryAdapter.deleteById(1L);

		// Assert
		verify(userJpaRepository, times(1)).deleteById(1L);
	}

	@Test
	void existsById_shouldReturnTrue_whenUserExists() {
		// Arrange
		when(userJpaRepository.existsById(1L)).thenReturn(true);

		// Act
		boolean exists = userRepositoryAdapter.existsById(1L);

		// Assert
		assertTrue(exists);
		verify(userJpaRepository, times(1)).existsById(1L);
	}

	@Test
	void existsById_shouldReturnFalse_whenUserDoesNotExist() {
		// Arrange
		when(userJpaRepository.existsById(999L)).thenReturn(false);

		// Act
		boolean exists = userRepositoryAdapter.existsById(999L);

		// Assert
		assertFalse(exists);
		verify(userJpaRepository, times(1)).existsById(999L);
	}

	@Test
	void existsByEmail_shouldReturnTrue_whenEmailExists() {
		// Arrange
		when(userJpaRepository.existsByEmail("test@example.com")).thenReturn(true);

		// Act
		boolean exists = userRepositoryAdapter.existsByEmail("test@example.com");

		// Assert
		assertTrue(exists);
		verify(userJpaRepository, times(1)).existsByEmail("test@example.com");
	}

	@Test
	void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
		// Arrange
		when(userJpaRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

		// Act
		boolean exists = userRepositoryAdapter.existsByEmail("nonexistent@example.com");

		// Assert
		assertFalse(exists);
		verify(userJpaRepository, times(1)).existsByEmail("nonexistent@example.com");
	}
}