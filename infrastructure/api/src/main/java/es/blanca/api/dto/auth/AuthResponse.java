package es.blanca.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
	private String token;
	private String type = "Bearer";
	private String email;
	private String role;

	public AuthResponse(String token, String email, String role) {
		this.token = token;
		this.email = email;
		this.role = role;
	}
}