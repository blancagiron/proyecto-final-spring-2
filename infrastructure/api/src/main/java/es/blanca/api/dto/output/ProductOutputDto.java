package es.blanca.api.dto.output;

import es.blanca.domain.model.ProductStatus;
import lombok.Data;


import java.time.LocalDateTime;

@Data
public class ProductOutputDto {
	private Long id;
	private String name;
	private Double price;
	private ProductStatus status;
	private LocalDateTime createdAt;
}
