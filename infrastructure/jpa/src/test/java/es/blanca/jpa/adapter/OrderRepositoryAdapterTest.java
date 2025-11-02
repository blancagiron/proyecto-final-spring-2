package es.blanca.jpa.adapter;

import es.blanca.domain.model.Order;
import es.blanca.domain.model.OrderStatus;
import es.blanca.jpa.entity.OrderEntity;
import es.blanca.jpa.mapper.OrderPersistenceMapper;
import es.blanca.jpa.repository.OrderJpaRepository;
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
class OrderRepositoryAdapterTest {

	@Mock
	private OrderJpaRepository orderJpaRepository;

	@Mock
	private OrderPersistenceMapper orderPersistenceMapper;

	@InjectMocks
	private OrderRepositoryAdapter orderRepositoryAdapter;

	private Order order;
	private OrderEntity orderEntity;

	@BeforeEach
	void setUp() {
		order = new Order();
		order.setId(1L);
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(LocalDateTime.now());

		orderEntity = new OrderEntity();
		orderEntity.setId(1L);
		orderEntity.setStatus(OrderStatus.PENDING);
		orderEntity.setCreatedAt(LocalDateTime.now());
	}

	@Test
	void save_shouldSaveOrder() {
		// Arrange
		when(orderPersistenceMapper.toEntity(any(Order.class))).thenReturn(orderEntity);
		when(orderJpaRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
		when(orderPersistenceMapper.toDomain(any(OrderEntity.class))).thenReturn(order);

		// Act
		Order savedOrder = orderRepositoryAdapter.save(order);

		// Assert
		assertNotNull(savedOrder);
		assertEquals(1L, savedOrder.getId());
		assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
		verify(orderJpaRepository, times(1)).save(orderEntity);
		verify(orderPersistenceMapper, times(1)).toEntity(order);
		verify(orderPersistenceMapper, times(1)).toDomain(orderEntity);
	}

	@Test
	void findById_shouldReturnOrder_whenExists() {
		// Arrange
		when(orderJpaRepository.findById(1L)).thenReturn(Optional.of(orderEntity));
		when(orderPersistenceMapper.toDomain(any(OrderEntity.class))).thenReturn(order);

		// Act
		Optional<Order> result = orderRepositoryAdapter.findById(1L);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(1L, result.get().getId());
		assertEquals(OrderStatus.PENDING, result.get().getStatus());
		verify(orderJpaRepository, times(1)).findById(1L);
	}

	@Test
	void findById_shouldReturnEmpty_whenNotExists() {
		// Arrange
		when(orderJpaRepository.findById(999L)).thenReturn(Optional.empty());

		// Act
		Optional<Order> result = orderRepositoryAdapter.findById(999L);

		// Assert
		assertFalse(result.isPresent());
		verify(orderJpaRepository, times(1)).findById(999L);
	}

	@Test
	void findByUserId_shouldReturnUserOrders() {
		// Arrange
		OrderEntity order2Entity = new OrderEntity();
		order2Entity.setId(2L);
		order2Entity.setStatus(OrderStatus.COMPLETED);

		Order order2 = new Order();
		order2.setId(2L);
		order2.setStatus(OrderStatus.COMPLETED);

		when(orderJpaRepository.findByUserId(1L)).thenReturn(Arrays.asList(orderEntity, order2Entity));
		when(orderPersistenceMapper.toDomain(orderEntity)).thenReturn(order);
		when(orderPersistenceMapper.toDomain(order2Entity)).thenReturn(order2);

		// Act
		List<Order> orders = orderRepositoryAdapter.findByUserId(1L);

		// Assert
		assertNotNull(orders);
		assertEquals(2, orders.size());
		verify(orderJpaRepository, times(1)).findByUserId(1L);
	}

	@Test
	void findByUserId_shouldReturnEmptyList_whenNoOrders() {
		// Arrange
		when(orderJpaRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

		// Act
		List<Order> orders = orderRepositoryAdapter.findByUserId(999L);

		// Assert
		assertNotNull(orders);
		assertTrue(orders.isEmpty());
		verify(orderJpaRepository, times(1)).findByUserId(999L);
	}

	@Test
	void findAll_shouldReturnAllOrders() {
		// Arrange
		OrderEntity order2Entity = new OrderEntity();
		order2Entity.setId(2L);

		Order order2 = new Order();
		order2.setId(2L);

		when(orderJpaRepository.findAll()).thenReturn(Arrays.asList(orderEntity, order2Entity));
		when(orderPersistenceMapper.toDomain(orderEntity)).thenReturn(order);
		when(orderPersistenceMapper.toDomain(order2Entity)).thenReturn(order2);

		// Act
		List<Order> orders = orderRepositoryAdapter.findAll();

		// Assert
		assertNotNull(orders);
		assertEquals(2, orders.size());
		verify(orderJpaRepository, times(1)).findAll();
	}

	@Test
	void findAll_shouldReturnEmptyList_whenNoOrders() {
		// Arrange
		when(orderJpaRepository.findAll()).thenReturn(Collections.emptyList());

		// Act
		List<Order> orders = orderRepositoryAdapter.findAll();

		// Assert
		assertNotNull(orders);
		assertTrue(orders.isEmpty());
		verify(orderJpaRepository, times(1)).findAll();
	}

	@Test
	void deleteById_shouldCallDelete() {
		// Arrange
		doNothing().when(orderJpaRepository).deleteById(1L);

		// Act
		orderRepositoryAdapter.deleteById(1L);

		// Assert
		verify(orderJpaRepository, times(1)).deleteById(1L);
	}

	@Test
	void existsById_shouldReturnTrue_whenExists() {
		// Arrange
		when(orderJpaRepository.existsById(1L)).thenReturn(true);

		// Act
		boolean exists = orderRepositoryAdapter.existsById(1L);

		// Assert
		assertTrue(exists);
		verify(orderJpaRepository, times(1)).existsById(1L);
	}

	@Test
	void existsById_shouldReturnFalse_whenNotExists() {
		// Arrange
		when(orderJpaRepository.existsById(999L)).thenReturn(false);

		// Act
		boolean exists = orderRepositoryAdapter.existsById(999L);

		// Assert
		assertFalse(exists);
		verify(orderJpaRepository, times(1)).existsById(999L);
	}
}