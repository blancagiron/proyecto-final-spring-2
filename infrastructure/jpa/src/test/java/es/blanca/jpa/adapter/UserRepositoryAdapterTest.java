package es.blanca.jpa.adapter;

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
import es.blanca.domain.exceptions.EntityNotFoundException; // Asegúrate de importar la excepción

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
		// Arrange
		user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");

		userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setEmail("test@example.com");
	}

	@Test
	void save_shouldSaveUser_whenUserIsValid() {
		// Arrange
		when(userPersistenceMapper.toEntity(any(User.class))).thenReturn(userEntity);

		// ✅ SOLUCIÓN: Añade este mock.
		// Prepara al repositorio para que cuando le pregunten por el ID 1, devuelva la entidad existente.
		when(userJpaRepository.findById(1L)).thenReturn(Optional.of(userEntity));

		when(userJpaRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		when(userPersistenceMapper.toDomain(any(UserEntity.class))).thenReturn(user);

		// Act
		User savedUser = userRepositoryAdapter.save(user);

		// Assert
		assertNotNull(savedUser);
		assertEquals(1L, savedUser.getId());
		// Verifica que se llamó a save
		verify(userJpaRepository, times(1)).save(userEntity);
		// Verifica que también se llamó a findById
		verify(userJpaRepository, times(1)).findById(1L);
	}

	// ... (El resto de tus tests no necesitan cambios)

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
	}

	@Test
	void findAll_shouldReturnAllUsers() {
		// Arrange
		when(userJpaRepository.findAll()).thenReturn(Collections.singletonList(userEntity));
		when(userPersistenceMapper.toDomain(any(UserEntity.class))).thenReturn(user);

		// Act
		List<User> users = userRepositoryAdapter.findAll();

		// Assert
		assertFalse(users.isEmpty());
		assertEquals(1, users.size());
	}

	@Test
	void deleteById_shouldCallDelete() {
		// Act
		userRepositoryAdapter.deleteById(1L);

		// Assert
		verify(userJpaRepository, times(1)).deleteById(1L);
	}
}