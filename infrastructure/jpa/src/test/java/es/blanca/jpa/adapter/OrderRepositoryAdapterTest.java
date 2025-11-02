package es.blanca.jpa.adapter;

import es.blanca.domain.model.Order;
import es.blanca.jpa.entity.OrderEntity;
import es.blanca.jpa.mapper.OrderPersistenceMapper;
import es.blanca.jpa.repository.OrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		// Arrange
		order = new Order();
		order.setId(1L);

		orderEntity = new OrderEntity();
		orderEntity.setId(1L);
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
	}

	@Test
	void findByUserId_shouldReturnUserOrders() {
		// Arrange
		when(orderJpaRepository.findByUserId(1L)).thenReturn(Collections.singletonList(orderEntity));
		when(orderPersistenceMapper.toDomain(any(OrderEntity.class))).thenReturn(order);

		// Act
		List<Order> orders = orderRepositoryAdapter.findByUserId(1L);

		// Assert
		assertFalse(orders.isEmpty());
		assertEquals(1, orders.size());
	}

	@Test
	void findAll_shouldReturnAllOrders() {
		// Arrange
		when(orderJpaRepository.findAll()).thenReturn(Collections.singletonList(orderEntity));
		when(orderPersistenceMapper.toDomain(any(OrderEntity.class))).thenReturn(order);

		// Act
		List<Order> orders = orderRepositoryAdapter.findAll();

		// Assert
		assertFalse(orders.isEmpty());
		assertEquals(1, orders.size());
	}

	@Test
	void deleteById_shouldCallDelete() {
		// Act
		orderRepositoryAdapter.deleteById(1L);

		// Assert
		verify(orderJpaRepository, times(1)).deleteById(1L);
	}
}