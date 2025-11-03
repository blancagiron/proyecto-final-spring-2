package es.blanca.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.blanca.api.dto.input.OrderInputDto;
import es.blanca.api.dto.output.OrderOutputDto;
import es.blanca.api.mapper.OrderApiMapper;
import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.exceptions.ForbiddenOperationException;
import es.blanca.domain.model.*;
import es.blanca.domain.port.OrderService;
import es.blanca.domain.port.ProductService;
import es.blanca.domain.port.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para OrderController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private OrderService orderService;

	@MockBean
	private OrderApiMapper orderApiMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private ProductService productService;

	private User testUser;
	private Order testOrder;
	private OrderOutputDto orderOutputDto;
	private OrderInputDto orderInputDto;
	private Product testProduct;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setEmail("test@example.com");
		testUser.setFullName("Test User");
		testUser.setRole(Role.ROLE_USER);
		testUser.setActive(true);

		testProduct = new Product();
		testProduct.setId(1L);
		testProduct.setName("Test Product");
		testProduct.setPrice(99.99);

		testOrder = new Order();
		testOrder.setId(1L);
		testOrder.setUser(testUser);
		testOrder.setStatus(OrderStatus.PENDING);
		testOrder.setCreatedAt(LocalDateTime.now());

		orderOutputDto = new OrderOutputDto();
		orderOutputDto.setId(1L);
		orderOutputDto.setUserId(1L);
		orderOutputDto.setStatus(OrderStatus.PENDING);

		orderInputDto = new OrderInputDto();
		orderInputDto.setUserId(1L);
		OrderInputDto.OrderProductInputDto orderProduct = new OrderInputDto.OrderProductInputDto();
		orderProduct.setProductId(1L);
		orderProduct.setAmount(2);
		orderInputDto.setOrderProducts(Arrays.asList(orderProduct));
	}

	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
	void createOrder_shouldReturn201_whenUserCreatesOwnOrder() throws Exception {
		// Arrange
		when(userService.findAll()).thenReturn(Arrays.asList(testUser));
		when(userService.findById(1L)).thenReturn(Optional.of(testUser));
		when(productService.findById(1L)).thenReturn(Optional.of(testProduct));
		when(orderService.create(any(Order.class))).thenReturn(testOrder);
		when(orderApiMapper.toOutputDto(any(Order.class))).thenReturn(orderOutputDto);

		// Act & Assert
		mockMvc.perform(post("/orders")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderInputDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.userId").value(1));

		verify(orderService, times(1)).create(any(Order.class));
	}

	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
	void createOrder_shouldReturn403_whenUserTriesToCreateOrderForOtherUser() throws Exception {
		// Arrange
		orderInputDto.setUserId(2L); // Intentando crear pedido para otro usuario
		when(userService.findAll()).thenReturn(Arrays.asList(testUser));

		// Act & Assert
		mockMvc.perform(post("/orders")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderInputDto)))
				.andExpect(status().isForbidden());

		verify(orderService, never()).create(any(Order.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createOrder_shouldReturn201_whenAdminCreatesOrderForAnyUser() throws Exception {
		// Arrange
		when(userService.findById(1L)).thenReturn(Optional.of(testUser));
		when(productService.findById(1L)).thenReturn(Optional.of(testProduct));
		when(orderService.create(any(Order.class))).thenReturn(testOrder);
		when(orderApiMapper.toOutputDto(any(Order.class))).thenReturn(orderOutputDto);

		// Act & Assert
		mockMvc.perform(post("/orders")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderInputDto)))
				.andExpect(status().isCreated());

		verify(orderService, times(1)).create(any(Order.class));
	}



	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
	void getAllOrders_shouldReturnOnlyUserOrders_whenUserIsNotAdmin() throws Exception {
		// Arrange
		when(userService.findAll()).thenReturn(Arrays.asList(testUser));
		when(orderService.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));
		when(orderApiMapper.toOutputDto(any(Order.class))).thenReturn(orderOutputDto);

		// Act & Assert
		mockMvc.perform(get("/orders")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1));

		verify(orderService, times(1)).findByUserId(1L);
		verify(orderService, never()).findAll();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getAllOrders_shouldReturnAllOrders_whenUserIsAdmin() throws Exception {
		// Arrange
		Order order2 = new Order();
		order2.setId(2L);

		OrderOutputDto output2 = new OrderOutputDto();
		output2.setId(2L);

		when(orderService.findAll()).thenReturn(Arrays.asList(testOrder, order2));
		when(orderApiMapper.toOutputDto(testOrder)).thenReturn(orderOutputDto);
		when(orderApiMapper.toOutputDto(order2)).thenReturn(output2);

		// Act & Assert
		mockMvc.perform(get("/orders")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2));

		verify(orderService, times(1)).findAll();
		verify(orderService, never()).findByUserId(any());
	}

	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
	void getOrderById_shouldReturnOrder_whenUserOwnsOrder() throws Exception {
		// Arrange
		when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
		when(userService.findAll()).thenReturn(Arrays.asList(testUser));
		when(orderApiMapper.toOutputDto(any(Order.class))).thenReturn(orderOutputDto);

		// Act & Assert
		mockMvc.perform(get("/orders/1")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));

		verify(orderService, times(1)).findById(1L);
	}

	@Test
	@WithMockUser(username = "test@example.com", roles = "USER")
	void getOrderById_shouldReturn403_whenUserDoesNotOwnOrder() throws Exception {
		// Arrange
		User otherUser = new User();
		otherUser.setId(2L);
		otherUser.setEmail("other@example.com");

		Order otherOrder = new Order();
		otherOrder.setId(2L);
		otherOrder.setUser(otherUser);

		when(orderService.findById(2L)).thenReturn(Optional.of(otherOrder));
		when(userService.findAll()).thenReturn(Arrays.asList(testUser));

		// Act & Assert
		mockMvc.perform(get("/orders/2")
						.with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getOrderById_shouldReturnOrder_whenAdminAccessesAnyOrder() throws Exception {
		// Arrange
		when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
		when(orderApiMapper.toOutputDto(any(Order.class))).thenReturn(orderOutputDto);

		// Act & Assert
		mockMvc.perform(get("/orders/1")
						.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));

		verify(orderService, times(1)).findById(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void getOrderById_shouldReturn404_whenOrderNotFound() throws Exception {
		// Arrange
		when(orderService.findById(999L))
				.thenThrow(new EntityNotFoundException("Order not found"));

		// Act & Assert
		mockMvc.perform(get("/orders/999")
						.with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateOrder_shouldReturn200_whenDataIsValid() throws Exception {
		// Arrange
		// ✅ CORRECCIÓN: Añadir mock de userService que el controller necesita
		when(userService.findById(1L)).thenReturn(Optional.of(testUser));
		doNothing().when(orderService).update(eq(1L), any(Order.class));
		when(orderService.findById(1L)).thenReturn(Optional.of(testOrder));
		when(orderApiMapper.toOutputDto(any(Order.class))).thenReturn(orderOutputDto);

		// Act & Assert
		mockMvc.perform(put("/orders/1")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderInputDto)))
				.andExpect(status().isOk());

		verify(orderService, times(1)).update(eq(1L), any(Order.class));
	}

	@Test
	@WithMockUser(roles = "USER")
	void updateOrder_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(put("/orders/1")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderInputDto)))
				.andExpect(status().isForbidden());

		verify(orderService, never()).update(any(), any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateOrder_shouldReturn404_whenOrderNotFound() throws Exception {
		// Arrange
		// ✅ CORRECCIÓN: Añadir mock de userService que el controller necesita
		when(userService.findById(1L)).thenReturn(Optional.of(testUser));
		doThrow(new EntityNotFoundException("Order not found"))
				.when(orderService).update(eq(999L), any(Order.class));

		// Act & Assert
		mockMvc.perform(put("/orders/999")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(orderInputDto)))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteOrder_shouldReturn204_whenOrderExists() throws Exception {
		// Arrange
		doNothing().when(orderService).delete(1L);

		// Act & Assert
		mockMvc.perform(delete("/orders/1")
						.with(csrf()))
				.andExpect(status().isNoContent());

		verify(orderService, times(1)).delete(1L);
	}

	@Test
	@WithMockUser(roles = "USER")
	void deleteOrder_shouldReturn403_whenUserIsNotAdmin() throws Exception {
		// Act & Assert
		mockMvc.perform(delete("/orders/1")
						.with(csrf()))
				.andExpect(status().isForbidden());

		verify(orderService, never()).delete(any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteOrder_shouldReturn404_whenOrderNotFound() throws Exception {
		// Arrange
		doThrow(new EntityNotFoundException("Order not found"))
				.when(orderService).delete(999L);

		// Act & Assert
		mockMvc.perform(delete("/orders/999")
						.with(csrf()))
				.andExpect(status().isNotFound());
	}
}