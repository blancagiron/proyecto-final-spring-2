package es.blanca.application;

import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.*;
import es.blanca.domain.port.OrderRepository;
import es.blanca.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private OrderServiceImpl orderService;

	private Order order;
	private User user;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");

		order = new Order();
		order.setId(1L);
		order.setUser(user);
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void findAll_shouldReturnAllOrders() {
		// Arrange
		Order order2 = new Order();
		order2.setId(2L);

		when(orderRepository.findAll()).thenReturn(Arrays.asList(order, order2));

		// Act
		List<Order> result = orderService.findAll();

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(orderRepository, times(1)).findAll();
	}

	@Test
	void findById_shouldReturnOrder_whenExists() {
		// Arrange
		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		// Act
		Optional<Order> result = orderService.findById(1L);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(1L, result.get().getId());
		verify(orderRepository, times(1)).findById(1L);
	}

	@Test
	void findById_shouldThrowException_whenNotExists() {
		// Arrange
		when(orderRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			orderService.findById(999L);
		});
	}

	@Test
	void findByUserId_shouldReturnUserOrders_whenUserExists() {
		// Arrange
		when(userRepository.existsById(1L)).thenReturn(true);
		when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(order));

		// Act
		List<Order> result = orderService.findByUserId(1L);

		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		verify(userRepository, times(1)).existsById(1L);
		verify(orderRepository, times(1)).findByUserId(1L);
	}

	@Test
	void findByUserId_shouldThrowException_whenUserNotExists() {
		// Arrange
		when(userRepository.existsById(999L)).thenReturn(false);

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			orderService.findByUserId(999L);
		});
		verify(orderRepository, never()).findByUserId(any());
	}

	@Test
	void create_shouldCreateOrder_whenUserExists() {
		// Arrange
		Order newOrder = new Order();
		newOrder.setUser(user);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		// Act
		Order result = orderService.create(newOrder);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getCreatedAt());
		assertEquals(OrderStatus.PENDING, result.getStatus());
		verify(userRepository, times(1)).findById(1L);
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	void create_shouldThrowException_whenUserNotFound() {
		// Arrange
		Order newOrder = new Order();
		newOrder.setUser(user);

		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			orderService.create(newOrder);
		});
		verify(orderRepository, never()).save(any());
	}

	@Test
	void update_shouldUpdateOrder_whenExists() {
		// Arrange
		Order existingOrder = new Order();
		existingOrder.setId(1L);
		existingOrder.setStatus(OrderStatus.PENDING);
		existingOrder.setUser(user);

		Order updateData = new Order();
		updateData.setStatus(OrderStatus.COMPLETED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
		when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

		// Act
		orderService.update(1L, updateData);

		// Assert
		verify(orderRepository, times(1)).findById(1L);
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	void update_shouldUpdateUser_whenNewUserProvided() {
		// Arrange
		Order existingOrder = new Order();
		existingOrder.setId(1L);
		existingOrder.setUser(user);

		User newUser = new User();
		newUser.setId(2L);

		Order updateData = new Order();
		updateData.setUser(newUser);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
		when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
		when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

		// Act
		orderService.update(1L, updateData);

		// Assert
		verify(userRepository, times(1)).findById(2L);
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	void update_shouldUpdateOrderProducts_whenProvided() {
		// Arrange
		Order existingOrder = new Order();
		existingOrder.setId(1L);

		OrderProduct orderProduct = new OrderProduct();
		orderProduct.setAmount(5);

		Order updateData = new Order();
		updateData.setOrderProducts(Arrays.asList(orderProduct));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
		when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

		// Act
		orderService.update(1L, updateData);

		// Assert
		verify(orderRepository, times(1)).save(any(Order.class));
	}

	@Test
	void update_shouldThrowException_whenOrderNotFound() {
		// Arrange
		when(orderRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			orderService.update(999L, order);
		});
		verify(orderRepository, never()).save(any());
	}

	@Test
	void update_shouldThrowException_whenNewUserNotFound() {
		// Arrange
		Order existingOrder = new Order();
		existingOrder.setId(1L);

		User newUser = new User();
		newUser.setId(999L);

		Order updateData = new Order();
		updateData.setUser(newUser);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			orderService.update(1L, updateData);
		});
		verify(orderRepository, never()).save(any());
	}

	@Test
	void delete_shouldDeleteOrder_whenExists() {
		// Arrange
		when(orderRepository.existsById(1L)).thenReturn(true);
		doNothing().when(orderRepository).deleteById(1L);

		// Act
		orderService.delete(1L);

		// Assert
		verify(orderRepository, times(1)).existsById(1L);
		verify(orderRepository, times(1)).deleteById(1L);
	}

	@Test
	void delete_shouldThrowException_whenOrderNotFound() {
		// Arrange
		when(orderRepository.existsById(999L)).thenReturn(false);

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			orderService.delete(999L);
		});
		verify(orderRepository, never()).deleteById(any());
	}
}