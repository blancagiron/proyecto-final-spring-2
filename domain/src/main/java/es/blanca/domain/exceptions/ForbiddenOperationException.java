package es.blanca.domain.exceptions;

public class ForbiddenOperationException extends RuntimeException {
	public ForbiddenOperationException(String message) {
		super(message);
	}
}
