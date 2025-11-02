package es.blanca.api.dto.output;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserOutputDto {
	private Long id;
	private String fullName;
	private String email;
	private LocalDateTime createdAt;
	private boolean isActive;
	private CountryOutputDto country;
	private List<OrderOutputDto> orders;
}
