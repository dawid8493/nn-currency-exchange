package pl.nn.currencyexchange.application.rest.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import pl.nn.currencyexchange.domain.enums.Currency;

@Data
@Builder
public class ExchangeMoneyCommand {

  @NotNull
  @Parameter(description = "Waluta którą chcemy nabyć", required = true)
  private Currency currency;

  @DecimalMin(value = "1")
  @Parameter(description = "Kwota docelowej waluty, którą chcemy nabyć")
  private BigDecimal amount;
}
