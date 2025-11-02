package es.blanca.jpa.adapter;

import es.blanca.domain.model.Order;
import es.blanca.domain.port.OrderRepository;
import es.blanca.jpa.entity.OrderEntity;
import es.blanca.jpa.mapper.OrderPersistenceMapper;
import es.blanca.jpa.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor

public class OrderRepositoryAdapter implements OrderRepository {

	private final OrderJpaRepository orderJpaRepository;
	private final OrderPersistenceMapper orderPersistenceMapper;

	@Override
	public List<Order> findByUserId(Long userId) {
		return orderJpaRepository.findByUserId(userId).stream()
				.map(orderPersistenceMapper::toDomain)
				.collect(Collectors.toList());
	}

	@Override
	public Order save(Order order) {
		OrderEntity orderEntity = orderPersistenceMapper.toEntity(order);
		OrderEntity orderEntitySaved = orderJpaRepository.save(orderEntity);
		return orderPersistenceMapper.toDomain(orderEntitySaved);
	}

	@Override
	public Optional<Order> findById(Long id) {
		return orderJpaRepository.findById(id).map(orderPersistenceMapper::toDomain);
	}

	@Override
	public List<Order> findAll() {
		return orderJpaRepository.findAll().stream()
				.map(orderPersistenceMapper::toDomain)
				.collect(Collectors.toList());
	}

	@Override
	public void deleteById(Long id) {
		orderJpaRepository.deleteById(id);
	}

	@Override
	public boolean existsById(Long id) {
		return orderJpaRepository.existsById(id);
	}
}
