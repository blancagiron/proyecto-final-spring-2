package es.blanca.api.dto.input;

import jakarta.validation.constraints.NotBlank;
import static es.blanca.api.config.Constants.*;
import lombok.Data;

@Data
public class UserCreateInputDto {

	@NotBlank(message = FULL_NAME_NOT_BLANK)
	private String fullName;
	@NotBlank(message = EMAIL_NOT_BLANK)
	private String email;
	@NotBlank(message = PASSWORD_NOT_BLANK)
	private String password;
}
