package es.blanca.api.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static es.blanca.api.config.Constants.*;

@Data
public class CountryInputDto {
	@NotBlank(message = COUNTRY_CODE_NOT_BLANK)
	@Size(min=COUNTRY_CODE_MIN_LENGTH, max=COUNTRY_CODE_MAX_LENGTH)
	private String code;

	@NotBlank(message = COUNTRY_NAME_NOT_BLANK)
	private String name;
}
