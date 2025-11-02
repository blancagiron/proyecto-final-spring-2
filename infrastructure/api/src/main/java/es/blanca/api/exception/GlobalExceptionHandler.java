package es.blanca.api.exception;

import es.blanca.domain.exceptions.EmailAlreadyExistsException;
import es.blanca.domain.exceptions.EntityNotFoundException;
import es.blanca.domain.exceptions.ForbiddenOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDate;

@ControllerAdvice
public class GlobalExceptionHandler {

	private CustomError buildError(String message, HttpStatus status) {
		return CustomError.builder()
				.timestamp(LocalDate.now())
				.httpCode(status.value())
				.message(message)
				.build();
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<CustomError> handleEntityNotFoundException(EntityNotFoundException ex) {
		CustomError error = buildError(ex.getMessage(), HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND); // 404
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<CustomError> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
		CustomError error = buildError(ex.getMessage(), HttpStatus.CONFLICT);
		return new ResponseEntity<>(error, HttpStatus.CONFLICT); // 409
	}

	@ExceptionHandler(ForbiddenOperationException.class)
	public ResponseEntity<CustomError> handleForbiddenOperationException(ForbiddenOperationException ex) {
		CustomError error = buildError(ex.getMessage(), HttpStatus.FORBIDDEN);
		return new ResponseEntity<>(error, HttpStatus.FORBIDDEN); // 403
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CustomError> handleValidationExceptions(MethodArgumentNotValidException ex) {
		CustomError error = buildError(ex.getBindingResult().getFieldError().getDefaultMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY); // 422
	}
}