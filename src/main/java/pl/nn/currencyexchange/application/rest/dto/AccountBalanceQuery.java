package pl.nn.currencyexchange.application.rest.dto;

import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import pl.nn.currencyexchange.domain.enums.Currency;

@Data
@Builder
public class AccountBalanceQuery {

  private Owner owner;
  private List<CurrencyBalance> wallet;

  @Data
  @Builder
  public static class Owner {

    private String firstName;
    private String lastName;
  }

  @Data
  @Builder
  public static class CurrencyBalance {

    private Currency currency;

    @Digits(fraction = 2, integer = 10)
    private BigDecimal amount;
  }
}
