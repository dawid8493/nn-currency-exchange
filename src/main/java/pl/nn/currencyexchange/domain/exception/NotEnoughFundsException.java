package pl.nn.currencyexchange.domain.exception;

public class NotEnoughFundsException extends RuntimeException {
  
  public NotEnoughFundsException(String message) {
    super(message);
  }
}
