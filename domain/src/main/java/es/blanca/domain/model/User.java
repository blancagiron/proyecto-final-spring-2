package es.blanca.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class User {
	private Long id;
	private Role role;
	private String fullName;
	private String email;
	private String password;
	private LocalDateTime createdAt;
	private boolean isActive = true;
	private Country country;
	private List<Order> orders;
}
