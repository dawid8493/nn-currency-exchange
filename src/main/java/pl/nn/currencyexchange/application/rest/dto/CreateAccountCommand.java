package pl.nn.currencyexchange.application.rest.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateAccountCommand {

  @NotEmpty
  @Parameter(description = "Imię użytkownika", required = true)
  private String firstName;

  @NotEmpty
  @Parameter(description = "Nazwisko użytkownika", required = true)
  private String lastName;

  @NotNull
  @Digits(fraction = 2, integer = 10)
  @DecimalMin(value = "1")
  @Parameter(description = "Saldo początkowe w PLN", required = true)
  private BigDecimal balance;
}
