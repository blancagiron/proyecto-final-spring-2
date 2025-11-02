package es.blanca.api.controller;

import es.blanca.api.dto.input.OrderInputDto;
import es.blanca.api.dto.output.OrderOutputDto;
import es.blanca.api.mapper.OrderApiMapper;
import es.blanca.domain.exceptions.ForbiddenOperationException;
import es.blanca.domain.model.Order;
import es.blanca.domain.model.OrderProduct;
import es.blanca.domain.model.Product;
import es.blanca.domain.model.User;
import es.blanca.domain.port.OrderService;
import es.blanca.domain.port.ProductService;
import es.blanca.domain.port.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final OrderApiMapper orderApiMapper;
	private final UserService userService;
	private final ProductService productService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<OrderOutputDto> createOrder(@Valid @RequestBody OrderInputDto dto) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUserEmail = authentication.getName();

		// Si es USER, solo puede crear pedidos para sí mismo
		if (authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
			User currentUser = userService.findAll().stream()
					.filter(u -> u.getEmail().equals(currentUserEmail))
					.findFirst()
					.orElseThrow();

			if (!currentUser.getId().equals(dto.getUserId())) {
				log.warn("User {} attempted to create order for another user", currentUserEmail);
				throw new ForbiddenOperationException("You can only create orders for yourself");
			}
		}

		log.info("Creating order for user: {}", dto.getUserId());

		// Crear Order
		Order order = new Order();
		User user = userService.findById(dto.getUserId()).orElseThrow();
		order.setUser(user);

		// Crear OrderProducts
		List<OrderProduct> orderProducts = dto.getOrderProducts().stream()
				.map(opDto -> {
					Product product = productService.findById(opDto.getProductId()).orElseThrow();
					OrderProduct op = new OrderProduct();
					op.setOrder(order);
					op.setProduct(product);
					op.setAmount(opDto.getAmount());
					return op;
				})
				.collect(Collectors.toList());

		order.setOrderProducts(orderProducts);

		Order createdOrder = orderService.create(order);
		return new ResponseEntity<>(orderApiMapper.toOutputDto(createdOrder), HttpStatus.CREATED);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<List<OrderOutputDto>> getAllOrders() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUserEmail = authentication.getName();

		List<Order> orders;

		// Si es USER, solo puede ver sus propios pedidos
		if (authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
			User currentUser = userService.findAll().stream()
					.filter(u -> u.getEmail().equals(currentUserEmail))
					.findFirst()
					.orElseThrow();

			log.info("Fetching orders for user: {}", currentUser.getId());
			orders = orderService.findByUserId(currentUser.getId());
		} else {
			// Si es ADMIN, puede ver todos los pedidos
			log.info("Fetching all orders (admin)");
			orders = orderService.findAll();
		}

		return ResponseEntity.ok(orders.stream()
				.map(orderApiMapper::toOutputDto)
				.collect(Collectors.toList()));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<OrderOutputDto> getOrderById(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUserEmail = authentication.getName();

		Order order = orderService.findById(id).orElseThrow();

		// Si es USER, solo puede ver sus propios pedidos
		if (authentication.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
			User currentUser = userService.findAll().stream()
					.filter(u -> u.getEmail().equals(currentUserEmail))
					.findFirst()
					.orElseThrow();

			if (!order.getUser().getId().equals(currentUser.getId())) {
				log.warn("User {} attempted to access order from another user", currentUserEmail);
				throw new ForbiddenOperationException("You can only view your own orders");
			}
		}

		log.info("Fetching order with id: {}", id);
		return ResponseEntity.ok(orderApiMapper.toOutputDto(order));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<OrderOutputDto> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderInputDto dto) {
		log.info("Attempting to update order with id: {}", id);

		Order orderToUpdate = new Order();
		if (dto.getUserId() != null) {
			User user = userService.findById(dto.getUserId()).orElseThrow();
			orderToUpdate.setUser(user);
		}

		orderService.update(id, orderToUpdate);
		Order updatedOrder = orderService.findById(id).orElseThrow();
		return ResponseEntity.ok(orderApiMapper.toOutputDto(updatedOrder));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteOrder(@PathVariable Long id) {
		log.info("Attempting to delete order with id: {}", id);
		orderService.delete(id);
	}
}