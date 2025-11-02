package es.blanca.jpa.repository;

import es.blanca.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity,Long> {
	List<OrderEntity> findByUserId(Long userId);
}
