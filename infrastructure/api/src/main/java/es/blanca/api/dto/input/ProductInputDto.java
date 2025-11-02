package es.blanca.api.dto.input;

import es.blanca.domain.model.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import static es.blanca.api.config.Constants.*;

@Data
public class ProductInputDto {
	@NotBlank(message = PRODUCT_NAME_NOT_BLANK)
	private String name;

	@NotNull(message = PRICE_NOT_NULL)
	@Positive(message = PRICE_POSITIVE)
	private Double price;

	@NotNull(message = STATUS_NOT_NULL)
	private ProductStatus status;
}
