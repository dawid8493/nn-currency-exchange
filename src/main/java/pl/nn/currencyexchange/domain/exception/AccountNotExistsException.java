package pl.nn.currencyexchange.domain.exception;

public class AccountNotExistsException extends RuntimeException {
  
  public AccountNotExistsException(String message) {
    super(message);
  }
}
