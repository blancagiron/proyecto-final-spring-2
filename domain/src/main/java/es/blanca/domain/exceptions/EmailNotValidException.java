package es.blanca.domain.exceptions;

public class EmailNotValidException extends RuntimeException {
	public EmailNotValidException(String message) {
		super(message);
	}
}
