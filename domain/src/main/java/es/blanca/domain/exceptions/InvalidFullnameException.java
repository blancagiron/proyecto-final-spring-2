package es.blanca.domain.exceptions;

public class InvalidFullnameException extends RuntimeException {
	public InvalidFullnameException(String message) {
		super(message);
	}
}
