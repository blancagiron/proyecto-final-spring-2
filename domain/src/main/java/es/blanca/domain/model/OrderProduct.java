package es.blanca.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {
	private Order order;
	private Product product;
	private Integer amount;
}
