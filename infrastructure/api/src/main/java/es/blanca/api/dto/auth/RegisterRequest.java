package es.blanca.api.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

import static es.blanca.api.config.Constants.*;

@Data
public class RegisterRequest {

	@NotBlank(message = FULL_NAME_NOT_BLANK)
	private String fullName;

	@NotBlank(message = EMAIL_NOT_BLANK)
	@Email(message = EMAIL_NOT_VALID)
	private String email;

	@NotBlank(message = PASSWORD_NOT_BLANK)
	@Size(min = 8, message = PASSWORD_SIZE)
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
			message = PASSWORD_PATTERN
	)
	private String password;
}