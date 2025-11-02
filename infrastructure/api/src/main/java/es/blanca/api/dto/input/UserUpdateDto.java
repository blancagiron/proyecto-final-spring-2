package es.blanca.api.dto.input;

import lombok.Data;

@Data
public class UserUpdateDto {
	private String fullName;
	private String email;
	private Boolean isActive;
}
