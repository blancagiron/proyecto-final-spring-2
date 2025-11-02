package es.blanca.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="orders_products")
public class OrderProductEntity  {

	@EmbeddedId
	private OrderProductId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("orderId")
	@JoinColumn(name = "order_id")
	private OrderEntity order;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("productId")
	@JoinColumn(name = "product_id")
	private ProductEntity product;

	@Column(nullable = false)
	private Integer amount;

}
