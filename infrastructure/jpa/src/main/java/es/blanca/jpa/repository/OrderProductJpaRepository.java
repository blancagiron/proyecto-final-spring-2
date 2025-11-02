package es.blanca.jpa.repository;

import es.blanca.jpa.entity.OrderProductEntity;
import es.blanca.jpa.entity.OrderProductId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<OrderProductEntity, OrderProductId> {
}
