package es.blanca.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	private Long id;
	private User user;
	private OrderStatus status;
	private LocalDateTime createdAt;
	private List<OrderProduct> orderProducts;
}
