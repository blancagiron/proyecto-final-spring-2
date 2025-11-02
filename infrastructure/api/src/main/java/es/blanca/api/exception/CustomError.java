package es.blanca.api.exception;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomError {
	private LocalDate timestamp;
	private int httpCode;
	private String message;
}