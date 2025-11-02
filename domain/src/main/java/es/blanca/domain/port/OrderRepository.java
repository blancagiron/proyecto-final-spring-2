package es.blanca.domain.port;

import es.blanca.domain.model.Order;

import java.util.List;

public interface OrderRepository extends  CrudRepository<Order,Long> {
	// find orders by users
	List<Order> findByUserId(Long userId);
}
