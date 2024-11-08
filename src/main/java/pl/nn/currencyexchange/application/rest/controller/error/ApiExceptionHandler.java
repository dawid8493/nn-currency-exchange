package pl.nn.currencyexchange.application.rest.controller.error;

import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import pl.nn.currencyexchange.domain.exception.AccountNotExistsException;
import pl.nn.currencyexchange.domain.exception.CurrencyExchangeRatesNotFoundException;
import pl.nn.currencyexchange.domain.exception.NotEnoughFundsException;

@ControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDto> handleValidationErrors(MethodArgumentNotValidException e) {
    String errorsMessage = e.getBindingResult().getFieldErrors().stream()
        .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
        .collect(Collectors.joining(";"));
    return handleException(HttpStatus.BAD_REQUEST, errorsMessage);
  }

  @ExceptionHandler(AccountNotExistsException.class)
  ResponseEntity<ErrorDto> handleAccountNotExists(AccountNotExistsException e) {
    return handleException(HttpStatus.NOT_FOUND, e);
  }

  @ExceptionHandler(CurrencyExchangeRatesNotFoundException.class)
  ResponseEntity<ErrorDto> handleCurrencyExchangeRatesNotFound(
      CurrencyExchangeRatesNotFoundException e) {
    return handleException(HttpStatus.NOT_FOUND, e);
  }

  @ExceptionHandler(NotEnoughFundsException.class)
  ResponseEntity<ErrorDto> handleNotEnoughFunds(NotEnoughFundsException e) {
    return handleException(HttpStatus.NOT_ACCEPTABLE, e);
  }

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<ErrorDto> handleExternalServiceNotFoundData(ResponseStatusException e) {
    return handleException(HttpStatus.NOT_FOUND, e);
  }

  private ResponseEntity<ErrorDto> handleException(HttpStatus status, String message) {
    var error = ErrorDto.builder()
        .code(status.value())
        .message(message)
        .build();
    return new ResponseEntity<>(error, status);
  }

  private ResponseEntity<ErrorDto> handleException(HttpStatus status, RuntimeException e) {
    var error = ErrorDto.builder()
        .code(status.value())
        .message(e.getMessage())
        .build();
    return new ResponseEntity<>(error, status);
  }

  @Data
  @Builder
  public static class ErrorDto {
    private int code;
    private String message;
  }
}
