package es.blanca.api.dto.output;

import es.blanca.domain.model.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderOutputDto {
	private Long id;
	private Long userId;
	private OrderStatus status;
	private LocalDateTime createdAt;
	private List<OrderProductOutputDto> orderProducts;

	@Data
	public static class OrderProductOutputDto {
		private Long productId;
		private String productName;
		private Integer amount;
	}
}
