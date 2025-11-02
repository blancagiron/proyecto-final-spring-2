package es.blanca.domain.port;

import es.blanca.domain.model.Order;

import java.util.List;

public interface OrderService extends CrudService<Order,Long> {
	// find orders by users id
	List<Order> findByUserId(Long userId);
}
