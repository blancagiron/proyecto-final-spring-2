package es.blanca.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	private Long id;
	private String name;
	private Double price;
	private ProductStatus status;
	private LocalDateTime createdAt;
}
