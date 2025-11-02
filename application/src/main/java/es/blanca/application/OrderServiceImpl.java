package es.blanca.application;

import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.model.Order;
import es.blanca.domain.model.OrderStatus;
import es.blanca.domain.model.User;
import es.blanca.domain.port.OrderRepository;
import es.blanca.domain.port.OrderService;
import es.blanca.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static es.blanca.application.config.ApplicationConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final UserRepository userRepository;

	@Override
	public List<Order> findByUserId(Long userId) {
		log.info("Trying to find orders by user id {}", userId);
		if(!userRepository.existsById(userId)){
			throw new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId));
		};
		return orderRepository.findByUserId(userId);
	}

	@Override
	public List<Order> findAll() {
		log.info("Fetching all orders");
		return orderRepository.findAll();
	}

	@Override
	public Optional<Order> findById(Long orderId) {
		log.info("Trying to find order by id {}", orderId);
		return Optional.of(orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException(String.format(ORDER_NOT_FOUND_BY_ID, orderId))));
	}

	@Override
	public Order create(Order order) {
		log.info("Trying to create order with id {}", order.getId() );
		Long userId = order.getUser().getId();
		User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, userId)));

		order.setUser(user);
		order.setCreatedAt(LocalDateTime.now());

		// initial status -> pending
		order.setStatus(OrderStatus.PENDING);
		return orderRepository.save(order);
	}

	@Override
	public void update(Long orderId, Order order) {
		log.info("Trying to update order with id {}", orderId);
		Order existingOrder = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException(String.format(ORDER_NOT_FOUND_BY_ID, orderId)));
		if(order.getStatus() != null){
			existingOrder.setStatus(order.getStatus());
		}
		if(order.getUser() != null && order.getUser().getId() != null){
			User newUser = userRepository.findById(order.getUser().getId()).orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND_BY_ID, order.getUser().getId())));
			existingOrder.setUser(newUser);
		}
		if(order.getOrderProducts() != null){
			existingOrder.setOrderProducts(order.getOrderProducts());
		}
		orderRepository.save(existingOrder);
		log.info("Order with id {} updated successfully", orderId);
	}

	@Override
	public void delete(Long orderId) {
		log.info("Trying to delete order with id {}", orderId);
		if(!orderRepository.existsById(orderId)){
			throw new EntityNotFoundException(String.format(ORDER_NOT_FOUND_BY_ID, orderId));
		}
		orderRepository.deleteById(orderId);
	}
}
