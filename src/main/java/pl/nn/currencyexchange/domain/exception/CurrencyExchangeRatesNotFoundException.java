package pl.nn.currencyexchange.domain.exception;

public class CurrencyExchangeRatesNotFoundException extends RuntimeException {
  
  public CurrencyExchangeRatesNotFoundException(String message) {
    super(message);
  }
}
