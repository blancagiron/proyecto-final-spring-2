package es.blanca.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.blanca.api.dto.input.UserCreateInputDto;
import es.blanca.api.dto.input.UserUpdateDto;
import es.blanca.api.dto.output.UserOutputDto;
import es.blanca.api.mapper.UserApiMapper;
import es.blanca.domain.exceptions.EmailAlreadyExistsException;
import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Role;
import es.blanca.domain.model.User;
import es.blanca.domain.port.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para UserController
 * Estos tests verifican:
 * - Operaciones CRUD de usuarios
 * - Control de acceso por roles
 * - Validaciones de entrada
 * - Manejo de errores
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private UserApiMapper userApiMapper;

	@MockBean
	private PasswordEncoder passwordEncoder;

	private User testUser;
	private UserOutputDto userOutputDto;
	private UserCreateInputDto createInputDto;
	private UserUpdateDto updateDto;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setEmail("test@example.com");
		testUser.setFullName("Test User");
		testUser.setPassword("hashedPassword");
		testUser.setRole(Role.ROLE_USER);
		testUser.setActive(true);
		testUser.setCreatedAt(LocalDateTime.now());

		userOutputDto = new UserOutputDto();
		userOutputDto.setId(1L);
		userOutputDto.setEmail("test@example.com");
		userOutputDto.setFullName("Test User");
		userOutputDto.setActive(true);

		createInputDto = new UserCreateInputDto();
		createInputDto.setEmail("new@example.com");
		createInputDto.setFullName("New User");
		createInputDto.setPassword("Password123");

		updateDto = new UserUpdateDto();
		updateDto.setFullName("Updated Name");
		updateDto.setEmail("updated@example.com");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createUser_shouldReturn201_whenDataIsValid() throws Exception {
		// Arrange
		when(userApiMapper.toDomain(any(UserCreateInputDto.class))).thenReturn(testUser);
		when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
		when(userService.create(any(User.class))).thenReturn(testUser);
		when(userApiMapper.toOutputDto(any(User.class))).thenReturn(userOutputDto);

		// Act & Assert
		mockMvc.perform(post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createInputDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.email").value("test@example.com"));

		verify(userService, times(1)).create(any(User.class));
	}

	@Test
	@WithMockUser(roles = "USER")
	void createUser_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createInputDto)))
				.andExpect(status().isForbidden());

		verify(userService, never()).create(any(User.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createUser_shouldReturn409_whenEmailAlreadyExists() throws Exception {
		// Arrange
		when(userApiMapper.toDomain(any(UserCreateInputDto.class))).thenReturn(testUser);
		when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
		when(userService.create(any(User.class)))
				.thenThrow(new EmailAlreadyExistsException("Email already exists"));

		// Act & Assert
		mockMvc.perform(post("/users")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createInputDto)))
				.andExpect(status().isConflict());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getAllUsers_shouldReturnUserList() throws Exception {
		// Arrange
		User user2 = new User();
		user2.setId(2L);
		user2.setEmail("user2@example.com");

		UserOutputDto output2 = new UserOutputDto();
		output2.setId(2L);
		output2.setEmail("user2@example.com");

		when(userService.findAll()).thenReturn(Arrays.asList(testUser, user2));
		when(userApiMapper.toOutputDto(testUser)).thenReturn(userOutputDto);
		when(userApiMapper.toOutputDto(user2)).thenReturn(output2);

		// Act & Assert
		mockMvc.perform(get("/users")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2));

		verify(userService, times(1)).findAll();
	}

	@Test
	@WithMockUser(roles = "USER")
	void getAllUsers_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(get("/users")
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(userService, never()).findAll();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getUserById_shouldReturnUser_whenExists() throws Exception {
		// Arrange
		when(userService.findById(1L)).thenReturn(Optional.of(testUser));
		when(userApiMapper.toOutputDto(any(User.class))).thenReturn(userOutputDto);

		// Act & Assert
		mockMvc.perform(get("/users/1")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.email").value("test@example.com"));

		verify(userService, times(1)).findById(1L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getUserById_shouldReturn404_whenNotExists() throws Exception {
		// Arrange
		when(userService.findById(999L))
				.thenThrow(new EntityNotFoundException("User not found"));

		// Act & Assert
		mockMvc.perform(get("/users/999")
						.with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateUser_shouldReturn200_whenDataIsValid() throws Exception {
		// Arrange
		when(userService.findById(1L)).thenReturn(Optional.of(testUser));
		doNothing().when(userService).update(eq(1L), any(User.class));
		when(userApiMapper.toOutputDto(any(User.class))).thenReturn(userOutputDto);

		// Act & Assert
		mockMvc.perform(put("/users/1")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isOk());

		verify(userService, times(1)).update(eq(1L), any(User.class));
	}

	@Test
	@WithMockUser(roles = "USER")
	void updateUser_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(put("/users/1")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isForbidden());

		verify(userService, never()).update(any(), any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateUser_shouldReturn404_whenUserNotFound() throws Exception {
		// Arrange
		when(userService.findById(999L))
				.thenThrow(new EntityNotFoundException("User not found"));

		// Act & Assert
		mockMvc.perform(put("/users/999")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDto)))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteUser_shouldReturn204_whenUserExists() throws Exception {
		// Arrange
		doNothing().when(userService).delete(1L);

		// Act & Assert
		mockMvc.perform(delete("/users/1")
						.with(csrf()))
				.andExpect(status().isNoContent());

		verify(userService, times(1)).delete(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void deleteUser_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(delete("/users/1")
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(userService, never()).delete(any());
	}

	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
	void assignCountry_shouldReturn200_whenUserUpdatesOwnCountry() throws Exception {
		// Arrange
		when(userService.findAll()).thenReturn(Arrays.asList(testUser));
		when(userService.assignCountry(1L, "ES")).thenReturn(Optional.of(testUser));
		when(userApiMapper.toOutputDto(any(User.class))).thenReturn(userOutputDto);

		// Act & Assert
		mockMvc.perform(patch("/users/1/country")
						.with(csrf())
						.param("countryCode", "ES"))
				.andExpect(status().isOk());

		verify(userService, times(1)).assignCountry(1L, "ES");
	}

	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
	void assignCountry_shouldReturn403_whenUserTriesToUpdateOtherUserCountry() throws Exception {
		// Arrange
		User otherUser = new User();
		otherUser.setId(2L);
		otherUser.setEmail("other@example.com");

		when(userService.findAll()).thenReturn(Arrays.asList(testUser));

		// Act & Assert
		mockMvc.perform(patch("/users/2/country")
						.with(csrf())
						.param("countryCode", "ES"))
				.andExpect(status().isForbidden());

		verify(userService, never()).assignCountry(any(), any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void assignCountry_shouldReturn200_whenAdminUpdatesAnyUserCountry() throws Exception {
		// Arrange
		when(userService.assignCountry(1L, "ES")).thenReturn(Optional.of(testUser));
		when(userApiMapper.toOutputDto(any(User.class))).thenReturn(userOutputDto);

		// Act & Assert
		mockMvc.perform(patch("/users/1/country")
						.with(csrf())
						.param("countryCode", "ES"))
				.andExpect(status().isOk());

		verify(userService, times(1)).assignCountry(1L, "ES");
	}
}