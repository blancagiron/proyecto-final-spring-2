package es.blanca.jpa.entity;

import es.blanca.domain.model.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="products")
public class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Double price;

	@Enumerated(EnumType.STRING)  // Store the enum name as a String in the database for better readability
	@Column(nullable = false, name="product_status")
	private ProductStatus status;

	@Column(nullable = false, name="creation_date")
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "product")
	private List<OrderProductEntity> orderProducts;
}
