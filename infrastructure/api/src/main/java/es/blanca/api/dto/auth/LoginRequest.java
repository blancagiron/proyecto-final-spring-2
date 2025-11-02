package es.blanca.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static es.blanca.api.config.Constants.*;

@Data
public class LoginRequest {

	@NotBlank(message = EMAIL_NOT_BLANK)
	@Email(message = EMAIL_NOT_VALID)
	private String email;

	@NotBlank(message = PASSWORD_NOT_BLANK)
	private String password;
}