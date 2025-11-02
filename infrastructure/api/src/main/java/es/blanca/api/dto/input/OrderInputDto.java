package es.blanca.api.dto.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

import static es.blanca.api.config.Constants.*;

@Data
public class OrderInputDto {

	@NotNull(message = USER_ID_NOT_NULL)
	private Long userId;

	@NotNull(message = PRODUCTS_NOT_EMPTY)
	@Valid
	private List< OrderProductInputDto> orderProducts;

	@Data
	public static class OrderProductInputDto {
		@NotNull(message = PRODUCT_ID_NOT_NULL)
		private Long productId;

		@NotNull(message = AMOUNT_NOT_NULL)
		@Positive(message = AMOUNT_POSITIVE)
		private Integer amount;
	}


}
